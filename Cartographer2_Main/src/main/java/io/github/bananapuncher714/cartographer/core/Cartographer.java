package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
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
import io.github.bananapuncher714.cartographer.core.api.configuration.YamlFileConfiguration;
import io.github.bananapuncher714.cartographer.core.command.CommandCartographer;
import io.github.bananapuncher714.cartographer.core.configuration.YamlMerger;
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
import io.github.bananapuncher714.nbteditor.NBTEditor;

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
	
	private PacketHandler handler;
	
	private MinimapManager mapManager;
	private PaletteManager paletteManager;
	private ModuleManager moduleManager;
	private DependencyManager dependencyManager;
	private PlayerManager playerManager;
	private LocaleManager localeManager;
	
	private CartographerSettings settings;
	
	private ForkJoinPool threadpool;
	
	// List of all running timers
	private Set< BukkitTask > tasks = new HashSet< BukkitTask >();
	
	// Contains all the renderers in use right now, with the key being map id assigned to each renderer
	private Map< Integer, CartographerRenderer > renderers = new HashMap< Integer, CartographerRenderer >();
	
	private CommandCartographer command;
	private PlayerListener playerListener;
	
	// Minimum tick limit allowed before pausing expensive operations
	// such as drawing the map, or loading chunks
	private int tickLimit = 18;
	
	private String user = "%%__USER__%%";
	private String nonce = "%%__NONCE__%%";
	
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
		Metrics metric = new Metrics( this, 6216 );
		
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
			loggerSevere( LocaleConstants.CORE_UNSUPPORTED_VERSION, NBTEditor.getMinecraftVersion() );
			loggerSevere( LocaleConstants.CORE_PLUGIN_DISABLE );
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}
		getLogger().info( "Detected version '" + NBTEditor.getMinecraftVersion() + "'" );
		
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			handler.inject( player );
		}
		
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
		try {
			moduleManager.loadModules();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
		
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			handler.uninject( player );
		}
		
		if ( threadpool != null ) {
			threadpool.shutdownNow();
		}
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
		if ( !isServerOverloaded() ) {
			mapManager.update();
		}
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
			FileUtil.saveToFile( getResource( "data/images/overlay.gif" ), new File( getDataFolder(), "images/overlay.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/background.gif" ), new File( getDataFolder(), "images/background.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/missing.png" ), new File( getDataFolder(), "images/missing.png" ), false );
			FileUtil.saveToFile( getResource( "data/images/disabled.png" ), new File( getDataFolder(), "images/disabled.png" ), false );
		}
		
		// Save the palettes because why not
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.13.2.yml" ), new File( PALETTE_DIR, "palette-1.13.2.yml" ), false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.11.2.yml" ), new File( PALETTE_DIR, "palette-1.11.2.yml" ), false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.12.2.yml" ), new File( PALETTE_DIR, "palette-1.12.2.yml" ), false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.15.1.yml" ), new File( PALETTE_DIR, "palette-1.15.1.yml" ), false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.16.1.yml" ), new File( PALETTE_DIR, "palette-1.16.1.yml" ), false );
		
		FileUtil.saveToFile( getResource( "README.md" ), README_FILE, true );
	}
	
	private void loadLocaleFiles() {
		// Disable this for now, I don't think anyone's going to complain about auto updating locale files
//		if ( !LOCALE_README_FILE.exists() ) {
			FileUtil.saveToFile( getResource( "data/locale/README.md" ), LOCALE_README_FILE, false );
			FileUtil.saveToFile( getResource( "data/locale/en_us.yml" ), new File( LOCALE_DIR, "en_us.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/en_pt.yml" ), new File( LOCALE_DIR, "en_pt.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/zh_cn.yml" ), new File( LOCALE_DIR, "zh_cn.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/cs_cz.yml" ), new File( LOCALE_DIR, "cs_cz.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/zh_tw.yml" ), new File( LOCALE_DIR, "zh_tw.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/de_de.yml" ), new File( LOCALE_DIR, "de_de.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/tr_tr.yml" ), new File( LOCALE_DIR, "tr_tr.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/fr_fr.yml" ), new File( LOCALE_DIR, "fr_fr.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/es_es.yml" ), new File( LOCALE_DIR, "es_es.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/it_it.yml" ), new File( LOCALE_DIR, "it_it.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/pl_pl.yml" ), new File( LOCALE_DIR, "pl_pl.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/nl_nl.yml" ), new File( LOCALE_DIR, "nl_nl.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/hu_hu.yml" ), new File( LOCALE_DIR, "hu_hu.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/fil_ph.yml" ), new File( LOCALE_DIR, "fil_ph.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ru_ru.yml" ), new File( LOCALE_DIR, "ru_ru.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ko_kr.yml" ), new File( LOCALE_DIR, "ko_kr.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/pt_br.yml" ), new File( LOCALE_DIR, "pt_br.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/pt_pt.yml" ), new File( LOCALE_DIR, "pt_pt.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ro_ro.yml" ), new File( LOCALE_DIR, "ro_ro.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/vi_vn.yml" ), new File( LOCALE_DIR, "vi_vn.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/id_id.yml" ), new File( LOCALE_DIR, "id_id.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ca_es.yml" ), new File( LOCALE_DIR, "ca_es.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/sk_sk.yml" ), new File( LOCALE_DIR, "sk_sk.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ja_jp.yml" ), new File( LOCALE_DIR, "ja_jp.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ka_ge.yml" ), new File( LOCALE_DIR, "ka_ge.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/da_dk.yml" ), new File( LOCALE_DIR, "da_dk.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/sr_sp.yml" ), new File( LOCALE_DIR, "sr_sp.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/sv_se.yml" ), new File( LOCALE_DIR, "sv_se.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/nb_no.yml" ), new File( LOCALE_DIR, "nb_no.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/ar_sa.yml" ), new File( LOCALE_DIR, "ar_sa.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/lv_lv.yml" ), new File( LOCALE_DIR, "lv_lv.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/lt_lt.yml" ), new File( LOCALE_DIR, "lt_lt.yml" ), false );
			FileUtil.saveToFile( getResource( "data/locale/pt_pt.yml" ), new File( LOCALE_DIR, "pt_pt.yml" ), false );
		        FileUtil.saveToFile( getResource( "data/locale/uk_ua.yml" ), new File( LOCALE_DIR, "uk_ua.yml" ), false );
//		}
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
		YamlFileConfiguration configuration = new YamlFileConfiguration( new File( getDataFolder() + "/" + "config.yml" ).toPath() );
		try {
			configuration.load();
			YamlMerger merger = new YamlMerger( configuration, getResource( "config.yml" ) );
			merger.updateHeader( false );
			merger.updateKeys();
			merger.trimKeys();
			merger.updateComments( false );
		} catch ( IOException | InvalidConfigurationException e ) {
			e.printStackTrace();
		}
		
		FileConfiguration config = configuration.getConfiguration();
		settings = new CartographerSettings();
		for ( String string : config.getStringList( "skip-ids" ) ) {
			settings.getInvalidIds().add( Integer.valueOf( string ) );
		}
		tickLimit = config.getInt( "tick-limit", 16 );
		settings.setRenderDelay( config.getInt( "render-delay", 1 ) );
		settings.setPaletteDebug( config.getBoolean( "palette-debug", false ) );
		settings.setRotateByDefault( config.getBoolean( "rotate-by-default", true ) );
		settings.setShownameByDefault( config.getBoolean( "showname-by-default", true ) );
		
		settings.setBlockUpdateDelay( config.getInt( "block-update.tick-delay", 5 ) );
		settings.setBlockUpdateAmount( config.getInt( "block-update.update-amount", 20 ) );

		settings.setPreventDrop( config.getBoolean( "prevent-drop", true ) );
		settings.setUseDropPacket( config.getBoolean( "use-drop-packet", true ) );
		
		settings.setRendererMultithread( config.getBoolean( "renderer.multithread.enabled", true ) );
		settings.setRendererThreadcount( config.getInt( "renderer.multithread.threadcount", 4 ) );
		settings.setUseSubtasks( config.getBoolean( "renderer.use-subtasks", false ) );
		
		OVERLAY_IMAGE = new File( getDataFolder() + "/" + config.getString( "images.overlay", "overlay.gif" ) );
		BACKGROUND_IMAGE = new File( getDataFolder() + "/" + config.getString( "images.background", "background.gif" ) );
		MISSING_MAP_IMAGE = new File( getDataFolder() + "/" + config.getString( "images.missing", "missing.png" ) );
		DISABLED_MAP_IMAGE = new File( getDataFolder() + "/" + config.getString( "images.disabled", "disabled.png" ) );
		
		settings.setDitherMissingMapImage( config.getBoolean( "dither-missing", false ) );
		
		localeManager.setDefaultLocale( config.getString( "default-locale", "default" ) );
		
		// Chunk load settings
		settings.setChunkUpdateDelay( config.getInt( "chunk.update-delay", 10 ) );
		ChunkLoadListener.INSTANCE.setForceLoad( config.getBoolean( "chunk.force-load", false ) );
		ChunkLoadListener.INSTANCE.setCacheAmount( config.getInt( "chunk.cache-per-update", 50 ) );
		ChunkLoadListener.INSTANCE.setLoadAmount( config.getInt( "chunk.load-per-update", 20 ) );
		ChunkLoadListener.INSTANCE.setGenerateAmount( config.getInt( "chunk.generate-per-update", 1 ) );
		
		for ( String string : config.getStringList( "blacklisted-inventories" ) ) {
			try {
				InventoryType invalid = InventoryType.valueOf( string );
				settings.getInvalidInventoryTypes().add( invalid );
				loggerInfo( LocaleConstants.CORE_ENABLE_CONFIG_INVENTORY_ADDED, string );
			} catch ( IllegalArgumentException exception ) {
				loggerWarning( LocaleConstants.CORE_ENABLE_CONFIG_INVENTORY_UNKNOWN, string );
			}
		}
		
		try {
			configuration.save();
		} catch ( IOException e ) {
			e.printStackTrace();
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
			DirectoryStream<Path> dirStream;
			try {
				dirStream = Files.newDirectoryStream( PALETTE_DIR.toPath() );
				for ( Path file : dirStream ) {
					if ( !Files.isDirectory( file ) ) {
						FileConfiguration configuration = YamlConfiguration.loadConfiguration( file.toFile() );
						MinimapPalette palette = paletteManager.load( configuration );
						
						String id = file.getFileName().toString().replaceAll( "\\.yml$", "" );
						paletteManager.register( id, palette );
						
						paletteManager.getLogger().infoTr( LocaleConstants.CORE_ENABLE_PALETTE_LOADING_DONE, id );
					}
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		} else {
			paletteManager.getLogger().warningTr( LocaleConstants.CORE_ENABLE_PALETTE_FOLDER_MISSING );
		}
	}
	
	private void loadImages() {
		try {
			if ( OVERLAY_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_OVERLAY_FOUND );
				settings.setOverlay( new SimpleImage( OVERLAY_IMAGE ) );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_OVERLAY_MISSING );
			}

			if ( BACKGROUND_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_BACKGROUND_FOUND );
				settings.setBackground( new SimpleImage( BACKGROUND_IMAGE ) );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_BACKGROUND_MISSING );
			}
			
			if ( MISSING_MAP_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_MISSING_FOUND );
				settings.setMissingMapImage( new SimpleImage( MISSING_MAP_IMAGE ) );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_MISSING_MISSING );
			}
			
			if ( DISABLED_MAP_IMAGE.exists() ) {
				loggerInfo( LocaleConstants.CORE_ENABLE_IMAGE_DISABLED_FOUND );
				settings.setDisabledMapImage( new SimpleImage( DISABLED_MAP_IMAGE ) );
			} else {
				loggerWarning( LocaleConstants.CORE_ENABLE_IMAGE_DISABLED_MISSING );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	private void loadMaps() {
		if ( MAP_DIR.exists() ) {
			try {
				DirectoryStream< Path > dirStream = Files.newDirectoryStream( MAP_DIR.toPath() );
				for ( Path file : dirStream ) {
					mapManager.constructNewMinimap( file.getFileName().toString() );
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadTimers() {
		tasks.forEach( t -> t.cancel() );
		tasks.clear();
		
		// Update the chunk listener sometime
		Bukkit.getScheduler().runTaskTimer( this, ChunkLoadListener.INSTANCE::update, 5, settings.getChunkUpdateDelay() );
		// Only run the block updater if it's more than 0 ticks of delay
		if ( settings.getBlockUpdateDelay() > 0 ) {
			Bukkit.getScheduler().runTaskTimer( this, playerListener::update, 1, settings.getBlockUpdateDelay() );
		}

		if ( threadpool != null ) {
			threadpool.shutdownNow();
		}
		threadpool = new ForkJoinPool( settings.getRendererThreadcount() );
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
	 * Does not load modules or minimaps
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
		File config = new File( dir + "/" + "config.yml" );
		FileUtil.saveToFile( getResource( "data/minimap-config.yml" ), config, false );
		YamlFileConfiguration configuration = new YamlFileConfiguration( config.toPath() );
		try {
			configuration.load();
			YamlMerger merger = new YamlMerger( configuration, getResource( "data/minimap-config.yml" ) );
			merger.updateHeader( false );
			merger.updateKeys();
			merger.trimKeys();
			merger.updateComments( false );
			configuration.save();
		} catch ( IOException | InvalidConfigurationException e ) {
			e.printStackTrace();
		}
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
	
	public CartographerSettings getSettings() {
		return settings;
	}
	
	public void setSettings( CartographerSettings settings ) {
		this.settings = settings;
	}
	
	public boolean isServerOverloaded() {
		return tickLimit > handler.getTPS();
	}
	
	public int getTickLimit() {
		return tickLimit;
	}
	
	public void setTickLimit( int limit ) {
		tickLimit = limit;
	}
	
	public ForkJoinPool getExecutorService() {
		return threadpool;
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
