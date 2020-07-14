package io.github.bananapuncher714.cartographer.core;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.command.CommandCartographer;
import io.github.bananapuncher714.cartographer.core.dependency.DependencyManager;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
import io.github.bananapuncher714.cartographer.core.locale.LocaleManager;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager.ColorType;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.ReflectionUtil;
import io.github.bananapuncher714.cartographer.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;

public class Cartographer extends JavaPlugin {
	private static Cartographer INSTANCE;
	
	private static File PALETTE_DIR;
	private static File MODULE_DIR;
	private static File MAP_DIR;
	private static File CACHE_DIR;
	private static File LOCALE_DIR;
	
	private static File README_FILE;
	private static File CONFIG_FILE;
	private static File DATA_FILE;
	private static File LOCALE_README_FILE;
	
	private static File MISSING_MAP_IMAGE;
	private static File OVERLAY_IMAGE;
	private static File BACKGROUND_IMAGE;
	private static File DISABLED_MAP_IMAGE;
	
	private TinyProtocol protocol;
	private PacketHandler handler;
	
	private MinimapManager mapManager;
	private PaletteManager paletteManager;
	private ModuleManager moduleManager;
	private DependencyManager dependencyManager;
	private PlayerManager playerManager;
	private LocaleManager localeManager;
	
	// Blacklist of map ids to NOT use
	private Set< Integer > invalidIds = new HashSet< Integer >();
	// Inventories that minimaps cannot be put into
	private Set< InventoryType > invalidInventoryTypes = new HashSet< InventoryType >();
	
	// List of all running timers
	private Set< BukkitTask > tasks = new HashSet< BukkitTask >();
	
	// Contains all the renderers in use right now, with the key being map id assigned to each renderer
	private Map< Integer, CartographerRenderer > renderers = new HashMap< Integer, CartographerRenderer >();
	
	private CommandCartographer command;
	private PlayerListener playerListener;
	
	// Minimum tick limit allowed before pausing expensive operations
	// such as drawing the map, or loading chunks
	private int tickLimit = 18;
	// How long in ticks to update the chunk listener
	private int chunkUpdateDelay = 10;
	// How long in ticks to update blocks on the map
	private int blockUpdateDelay = 10;
	// How long in ticks until the map can be updated again
	private int renderDelay;
	// Global default for rotation setting
	private boolean rotateByDefault = true;
	// Print out debug information regarding missing colors and materials in the console
	private boolean paletteDebug;
	// Catch the drop item packet
	private boolean preventDrop = true;
	private boolean packetDrop = true;
	// Dither the missing map image
	private boolean ditherMissing = false;
	
	private SimpleImage loadingBackground;
	private SimpleImage overlay;
	private SimpleImage missingMapImage;
	private SimpleImage disabledMap;
	
	// If the server has been completely loaded. Something to do with modules and plugins and dependencies
	private boolean loaded = false;
	
	static {
		// Disable java.awt.AWTError: Assistive Technology not found: org.GNOME.Accessibility.AtkWrapper from showing up
		System.setProperty( "javax.accessibility.assistive_technologies", " " );
		
		// No GUI present, so we want to enforce that
		System.setProperty( "java.awt.headless", "true" );
	}
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		// bStats
		Metrics metric = new Metrics( this );
		
		PALETTE_DIR = new File( getDataFolder() + "/" + "palettes/" );
		MODULE_DIR = new File( getDataFolder() + "/" + "modules/" );
		MAP_DIR = new File( getDataFolder() + "/" + "maps/" );
		CACHE_DIR = new File( getDataFolder() + "/" + "players/" );
		LOCALE_DIR = new File( getDataFolder() + "/" + "locale/" );
		
		README_FILE = new File( getDataFolder(), "README.md" );
		CONFIG_FILE = new File( getDataFolder(), "config.yml" );
		DATA_FILE = new File( getDataFolder(), "data.yml" );
		LOCALE_README_FILE = new File( LOCALE_DIR, "README.md" );
		
		JetpImageUtil.init();
		
		// Save the locale files
		loadLocaleFiles();
		
		paletteManager = new PaletteManager( this );
		mapManager = new MinimapManager( this );
		moduleManager = new ModuleManager( this, MODULE_DIR );
		playerManager = new PlayerManager( this, CACHE_DIR );
		dependencyManager = new DependencyManager( this );
		localeManager = new LocaleManager( this, LOCALE_DIR );
		
		getLogger().info( "Loading locales..." );
		localeManager.reload();
		
		handler = ReflectionUtil.getNewPacketHandlerInstance();
		if ( handler == null ) {
			loggerSevere( LocaleConstants.CORE_UNSUPPORTED_VERSION, ReflectionUtil.VERSION );
			loggerSevere( LocaleConstants.CORE_PLUGIN_DISABLE );
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}
		
		protocol = new TinyProtocol( this ) {
			@Override
			public Object onPacketOutAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptOut( player, packet );
			}

			@Override
			public Object onPacketInAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptIn( player, packet );
			}
		};
		
		// Create our base command
		command = new CommandCartographer( this, getCommand( "cartographer" ) );
		playerListener = new PlayerListener( this );
		
		Bukkit.getPluginManager().registerEvents( playerListener, this );
		Bukkit.getPluginManager().registerEvents( new MapListener( this ), this );
		Bukkit.getPluginManager().registerEvents( ChunkLoadListener.INSTANCE, this );
		Bukkit.getPluginManager().registerEvents( new CartographerListener(), this );
		
		// Start loading everything sequentially
		load();
		
		Bukkit.getOnlinePlayers().stream().map( Player::getUniqueId ).forEach( playerManager::getViewerFor );
		
		// Load the modules in beforehand
		moduleManager.loadModules();
	}
	
	@Override
	public void onDisable() {
		loggerInfo( LocaleConstants.CORE_DISABLE_MODULES_DISABLE );
		moduleManager.terminate();
		
		for ( CartographerRenderer renderer : renderers.values() ) {
			renderer.terminate();
		}
		loggerInfo( LocaleConstants.CORE_DISABLE_SAVING_MAP_START );
		mapManager.terminate();
		loggerInfo( LocaleConstants.CORE_DISABLE_SAVING_MAP_FINISH );
		saveData();
		
		loggerInfo( LocaleConstants.CORE_DISABLE_SAVING_PLAYER_START );
		Bukkit.getOnlinePlayers().stream().map( Player::getUniqueId ).forEach( playerManager::unload );
		loggerInfo( LocaleConstants.CORE_DISABLE_SAVING_PLAYER_FINISH );
	}
	
	protected void onServerLoad() {
		// Don't load things again, just once
		if ( loaded ) {
			return;
		}
		loaded = true;
		
		Bukkit.getScheduler().runTaskTimer( this, this::update, 5, 20 );
		
		// Enable the modules afterwards
		loggerInfo( LocaleConstants.CORE_ENABLE_MODULES_ENABLE );
		moduleManager.enableModules();
		
		loadAfter();
	}
	
	private void update() {
		mapManager.update();
	}
	
	private void saveData() {
		if ( !DATA_FILE.exists() ) {
			try {
				DATA_FILE.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
				return;
			}
		}
		FileConfiguration data = YamlConfiguration.loadConfiguration( DATA_FILE );
		
		for ( int mapId : renderers.keySet() ) {
			CartographerRenderer renderer = renderers.get( mapId );
			
			String id = "MISSING MAP";
			Minimap map = renderer.getMinimap();
			if ( map != null ) {
				id = map.getId();
			}
			
			data.set( "custom-renderer-ids." + mapId, id );
		}
		
		try {
			data.save( DATA_FILE );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
	}
	
	private void load() {
		// Load all required files first
		loadInit();
		
		// Load the config and images first
		loggerInfo( LocaleConstants.CORE_ENABLE_LOAD_CONFIG );
		loadConfig();
		loggerInfo( LocaleConstants.CORE_ENABLE_LOAD_IMAGES );
		loadImages();
		
		// Load the palettes
		loggerInfo( LocaleConstants.CORE_ENABLE_LOAD_PALETTES );
		loadPalettes();
		
		// Load the runnables
		loadTimers();
	}
	
	private void loadAfter() {
		loggerInfo( LocaleConstants.CORE_ENABLE_LOAD_DATA );
		
		// Load the maps
		// Requires palettes
		loadMaps();
		
		// Load the data
		// Requires maps
		loadData();
	}
	
	private void loadInit() {
		FileUtil.saveToFile( getResource( "config.yml" ), CONFIG_FILE, false );
		if ( !README_FILE.exists() ) {
			FileUtil.updateConfigFromFile( CONFIG_FILE, getResource( "config.yml" ) );
			FileUtil.saveToFile( getResource( "data/images/overlay.gif" ), new File( getDataFolder(), "overlay.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/background.gif" ), new File( getDataFolder(), "background.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/missing.png" ), new File( getDataFolder(), "missing.png" ), false );
			FileUtil.saveToFile( getResource( "data/images/disabled.png" ), new File( getDataFolder(), "disabled.png" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.13.2.yml" ), new File( PALETTE_DIR, "palette-1.13.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.11.2.yml" ), new File( PALETTE_DIR, "palette-1.11.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.12.2.yml" ), new File( PALETTE_DIR, "palette-1.12.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.15.1.yml" ), new File( PALETTE_DIR, "palette-1.15.1.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.16.1.yml" ), new File( PALETTE_DIR, "palette-1.16.1.yml" ), false );
		}
		
		FileUtil.saveToFile( getResource( "README.md" ), README_FILE, true );
	}
	
	private void loadLocaleFiles() {
		if ( !LOCALE_README_FILE.exists() ) {
			FileUtil.saveToFile( getResource( "data/locale/README.md" ), LOCALE_README_FILE, false );
			FileUtil.saveToFile( getResource( "data/locale/en_us.yml" ), new File( LOCALE_DIR, "en_us.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/en_pt.yml" ), new File( LOCALE_DIR, "en_pt.yml" ), false );
		}
	}
	
	private void loadData() {
		if ( DATA_FILE.exists() ) {
			FileConfiguration data = YamlConfiguration.loadConfiguration( DATA_FILE );
			if ( data.contains( "custom-renderer-ids" ) ) {
				for ( String key : data.getConfigurationSection( "custom-renderer-ids" ).getKeys( false ) ) {
					String id = data.getString( "custom-renderer-ids." + key );
					int mapId = Integer.parseInt( key );
					Minimap map = mapManager.getMinimaps().get( id );
					MapView view = handler.getUtil().getMap( mapId );
					
					// Do not want to convert maps that do not exist
					if ( map != null && view != null ) {
						mapManager.convert( view, map );
					}
				}
			}
		}
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder() + "/" + "config.yml" ) );
		for ( String string : config.getStringList( "skip-ids" ) ) {
			invalidIds.add( Integer.valueOf( string ) );
		}
		tickLimit = config.getInt( "tick-limit", 16 );
		renderDelay = config.getInt( "render-delay", 1 );
		paletteDebug = config.getBoolean( "palette-debug", false );
		rotateByDefault = config.getBoolean( "rotate-by-default", true );

		blockUpdateDelay = config.getInt( "block-update-tick-delay", 5 );
		
		preventDrop = config.getBoolean( "prevent-drop", true );
		packetDrop = config.getBoolean( "use-drop-packet", true );
		
		ditherMissing = config.getBoolean( "dither-missing", false );
		
		localeManager.setDefaultLocale( config.getString( "default-locale", "default" ) );
		
		// Chunk load settings
		chunkUpdateDelay = config.getInt( "chunk.update-delay", 10 );
		ChunkLoadListener.INSTANCE.setForceLoad( config.getBoolean( "chunk.force-load", false ) );
		ChunkLoadListener.INSTANCE.setCacheAmount( config.getInt( "chunk.cache-per-update", 50 ) );
		ChunkLoadListener.INSTANCE.setLoadAmount( config.getInt( "chunk.load-per-update", 20 ) );
		ChunkLoadListener.INSTANCE.setGenerateAmount( config.getInt( "chunk.generate-per-update", 1 ) );
		
		for ( String string : config.getStringList( "blacklisted-inventories" ) ) {
			try {
				InventoryType invalid = InventoryType.valueOf( string );
				invalidInventoryTypes.add( invalid );
				loggerInfo( LocaleConstants.CORE_ENABLE_CONFIG_INVENTORY_ADDED, string );
			} catch ( IllegalArgumentException exception ) {
				loggerWarning( LocaleConstants.CORE_ENABLE_CONFIG_INVENTORY_UNKNOWN, string );
			}
		}
	}
	
	private void loadPalettes() {
		paletteManager.getLogger().infoTr( LocaleConstants.CORE_ENABLE_PALETTE_VANILLA_CREATE );
		MinimapPalette vanilla = handler.getVanillaPalette();
		paletteManager.register( "default", vanilla );
		
		File vanillaPalette = new File( PALETTE_DIR + "/" + "vanilla.yml" );
		if ( !vanillaPalette.exists() ) {
			paletteManager.getLogger().warningTr( LocaleConstants.CORE_ENABLE_PALETTE_VANILLA_MISSING );
			
			PALETTE_DIR.mkdirs();
			try {
				vanillaPalette.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			
			FileConfiguration vanillaConfig = YamlConfiguration.loadConfiguration( vanillaPalette );
			paletteManager.save( vanilla, vanillaConfig, ColorType.RGB );
			
			try {
				vanillaConfig.save( vanillaPalette );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		
		paletteManager.getLogger().infoTr( LocaleConstants.CORE_ENABLE_PALETTE_VANILLA_MAPPED, vanilla.getMaterials().size() + vanilla.getTransparentBlocks().size() );
		
		paletteManager.getLogger().infoTr( LocaleConstants.CORE_ENABLE_PALETTE_LOADING );
		if ( PALETTE_DIR.exists() ) {
			for ( File file : PALETTE_DIR.listFiles() ) {
				if ( !file.isDirectory() ) {
					FileConfiguration configuration = YamlConfiguration.loadConfiguration( file );
					MinimapPalette palette = paletteManager.load( configuration );
					
					String id = file.getName().replaceAll( "\\.yml$", "" );
					paletteManager.register( id, palette );
					
					paletteManager.getLogger().infoTr( LocaleConstants.CORE_ENABLE_PALETTE_LOADING_DONE, id );
				}
			}
		} else {
			paletteManager.getLogger().warningTr( LocaleConstants.CORE_ENABLE_PALETTE_FOLDER_MISSING );
		}
	}
	
	private void loadImages() {
		OVERLAY_IMAGE = FileUtil.getImageFile( getDataFolder(), "overlay" );
		BACKGROUND_IMAGE = FileUtil.getImageFile( getDataFolder(), "background" );
		MISSING_MAP_IMAGE = FileUtil.getImageFile( getDataFolder(), "missing" );
		DISABLED_MAP_IMAGE = FileUtil.getImageFile( getDataFolder(), "disabled" );
		
		try {
			if ( OVERLAY_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_OVERLAY_FOUND );
				this.overlay = new SimpleImage( OVERLAY_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_OVERLAY_MISSING );
			}

			if ( BACKGROUND_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_BACKGROUND_FOUND );
				this.loadingBackground = new SimpleImage( BACKGROUND_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_BACKGROUND_MISSING );
			}
			
			if ( MISSING_MAP_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_MISSING_FOUND );
				missingMapImage = new SimpleImage( MISSING_MAP_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_MISSING_MISSING );
			}
			
			if ( DISABLED_MAP_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_DISABLED_FOUND );
				disabledMap = new SimpleImage( DISABLED_MAP_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_DISABLED_MISSING );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	private void loadMaps() {
		if ( MAP_DIR.exists() ) {
			for ( File file : MAP_DIR.listFiles() ) {
				mapManager.constructNewMinimap( file.getName() );
			}
		}
	}
	
	private void loadTimers() {
		tasks.forEach( t -> t.cancel() );
		tasks.clear();
		
		// Update the chunk listener sometime
		Bukkit.getScheduler().runTaskTimer( this, ChunkLoadListener.INSTANCE::update, 5, chunkUpdateDelay );
		// Only run the block updater if it's more than 0 ticks of delay
		if ( chunkUpdateDelay > 0 ) {
			Bukkit.getScheduler().runTaskTimer( this, playerListener::update, 1, blockUpdateDelay );
		}
	}
	
	private void loggerInfo( String key, Object... params ) {
		String message = localeManager.translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			getLogger().info( message );
		}
	}
	
	private void loggerWarning( String key, Object... params ) {
		String message = localeManager.translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			getLogger().warning( message );
		}
	}
	
	private void loggerSevere( String key, Object... params ) {
		String message = localeManager.translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			getLogger().severe( message );
		}
	}
	
	/**
	 * Purely for configs, palettes and images
	 * Does not load modules
	 */
	public void reload() {
		// Re-save all locale files
		loadLocaleFiles();
		
		// Reload the locale manager before all
		localeManager.reload();
		
		load();
	}
	
	public File getMapDirFor( String id ) {
		return new File( MAP_DIR + "/" + id );
	}
	
	public File getAndConstructMapDir( String id ) {
		File dir = new File( MAP_DIR + "/" + id );
		saveMapFiles( dir );
		
		return dir;
	}
	
	protected void saveMapFiles( File dir ) {
		FileUtil.saveToFile( getResource( "data/minimap-config.yml" ), new File( dir + "/" + "config.yml" ), false );
	}
	
	public TinyProtocol getProtocol() {
		return protocol;
	}
	
	public PacketHandler getHandler() {
		return handler;
	}
	
	public CommandCartographer getCommand() {
		return command;
	}
	
	public MinimapManager getMapManager() {
		return mapManager;
	}
	
	public PaletteManager getPaletteManager() {
		return paletteManager;
	}
	
	public ModuleManager getModuleManager() {
		return moduleManager;
	}
	
	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}
	
	public PlayerManager getPlayerManager() {
		return playerManager;
	}
	
	public LocaleManager getLocaleManager() {
		return localeManager;
	}
	
	public Map< Integer, CartographerRenderer > getRenderers() {
		return renderers;
	}
	
	protected Set< Integer > getInvalidIds() {
		return invalidIds;
	}
	
	public int getRenderDelay() {
		return renderDelay;
	}
	
	public boolean isServerOverloaded() {
		return tickLimit > handler.getTPS();
	}
	
	public boolean isRotateByDefault() {
		return rotateByDefault;
	}
	
	public boolean isPaletteDebug() {
		return paletteDebug;
	}
	
	public boolean isPreventDrop() {
		return preventDrop;
	}
	
	public boolean isUseDropPacket() {
		return packetDrop;
	}
	
	public boolean isDitherMissingMapImage() {
		return ditherMissing;
	}
	
	public int getBlockUpdateDelay() {
		return blockUpdateDelay;
	}
	
	public SimpleImage getBackground() {
		// TODO Specify that this is 128x128
		return loadingBackground;
	}
	
	public SimpleImage getOverlay() {
		// TODO Specify that this is 128x128
		return overlay;
	}

	public SimpleImage getMissingMapImage() {
		return missingMapImage;
	}
	
	public SimpleImage getDisabledMapImage() {
		return disabledMap;
	}
	
	public boolean isValidInventory( InventoryType type ) {
		return !invalidInventoryTypes.contains( type );
	}
	
	public static Cartographer getInstance() {
		return INSTANCE;
	}
	
	public static GeneralUtil getUtil() {
		return getInstance().getHandler().getUtil();
	}
	
	public static File getMapSaveDir() {
		return MAP_DIR;
	}
	
	public static File getModuleDir() {
		return MODULE_DIR;
	}
	
	public static File getPaletteDir() {
		return PALETTE_DIR;
	}
	
	public static File getCacheDir() {
		return CACHE_DIR;
	}
}
