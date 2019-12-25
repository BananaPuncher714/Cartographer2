package io.github.bananapuncher714.cartographer.core;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
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
	
	private static File README_FILE;
	private static File CONFIG_FILE;
	private static File DATA_FILE;
	
	private static File MISSING_MAP_IMAGE;
	private static File OVERLAY_IMAGE;
	private static File BACKGROUND_IMAGE;
	
	private TinyProtocol protocol;
	private PacketHandler handler;
	
	private MinimapManager mapManager;
	private PaletteManager paletteManager;
	private ModuleManager moduleManager;
	
	private Set< Integer > invalidIds = new HashSet< Integer >();
	private Set< InventoryType > invalidInventoryTypes = new HashSet< InventoryType >();
	
	private Map< Integer, CartographerRenderer > renderers = new HashMap< Integer, CartographerRenderer >();
	
	private CartographerCommand command;
	
	private int chunksPerSecond = 1;
	private boolean forceLoad = false;
	private boolean rotateByDefault = true;
	
	private SimpleImage loadingBackground;
	private SimpleImage overlay;
	private SimpleImage missingMapImage;
	
	private boolean loaded = false;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		// Disable java.awt.AWTError: Assistive Technology not found: org.GNOME.Accessibility.AtkWrapper from showing up
		Properties props = System.getProperties();
		props.setProperty( "javax.accessibility.assistive_technologies", "" );
		
		
		PALETTE_DIR = new File( getDataFolder() + "/" + "palettes/" );
		MODULE_DIR = new File( getDataFolder() + "/" + "modules/" );
		MAP_DIR = new File( getDataFolder() + "/" + "maps/" );
		
		README_FILE = new File( getDataFolder() + "/" + "README.md" );
		CONFIG_FILE = new File( getDataFolder() + "/" + "config.yml" );
		DATA_FILE = new File( getDataFolder() + "/" + "data.yml" );
		
		JetpImageUtil.init();
		
		handler = ReflectionUtil.getNewPacketHandlerInstance();
		if ( handler == null ) {
			getLogger().severe( "This version(" + ReflectionUtil.VERSION + ") is not supported currently!" );
			getLogger().severe( "Disabling..." );
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
		
		paletteManager = new PaletteManager( this );
		mapManager = new MinimapManager( this );
		moduleManager = new ModuleManager( this, MODULE_DIR );
		
		command = new CartographerCommand();
		getCommand( "cartographer" ).setExecutor( command );
		getCommand( "cartographer" ).setTabCompleter( command );
		
		Bukkit.getScheduler().runTaskTimer( this, ChunkLoadListener.INSTANCE::update, 5, 10 );
		
		Bukkit.getPluginManager().registerEvents( new PlayerListener( this ), this );
		Bukkit.getPluginManager().registerEvents( new MapListener( this ), this );
		Bukkit.getPluginManager().registerEvents( ChunkLoadListener.INSTANCE, this );
		Bukkit.getPluginManager().registerEvents( new CartographerListener(), this );
		
		load();
		
		// Load the modules in beforehand
		moduleManager.loadModules();
	}
	
	@Override
	public void onDisable() {
		getLogger().info( "Disabling modules..." );
		moduleManager.disableModules();
		
		for ( CartographerRenderer renderer : renderers.values() ) {
			renderer.terminate();
		}
		getLogger().info( "Saving map data. This may take a while..." );
		mapManager.terminate();
		getLogger().info( "Saving map data complete!" );
		saveData();
	}
	
	protected void onServerLoad() {
		if ( loaded ) {
			return;
		}
		loaded = true;
		Bukkit.getScheduler().runTaskTimer( this, this::update, 5, 1 );
		
		// Enable the modules afterwards
		getLogger().info( "Enabling modules..." );
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
		getLogger().info( "Loading config..." );
		loadConfig();
		getLogger().info( "Loading images..." );
		loadImages();
		
		// Load the palettes
		getLogger().info( "Loading palettes..." );
		loadPalettes();
	}
	
	private void loadAfter() {
		// Load the maps
		// Requires palettes
		loadMaps();
		
		// Load the data
		// Requires maps
		loadData();
	}
	
	private void loadInit() {
		if ( !README_FILE.exists() ) {
			FileUtil.saveToFile( getResource( "README.md" ), README_FILE, false );
			FileUtil.saveToFile( getResource( "config.yml" ), CONFIG_FILE, false );
			FileUtil.updateConfigFromFile( CONFIG_FILE, getResource( "config.yml" ) );
			FileUtil.saveToFile( getResource( "data/images/overlay.gif" ), new File( getDataFolder() + "/" + "overlay.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/background.gif" ), new File( getDataFolder() + "/" + "background.gif" ), false );
			FileUtil.saveToFile( getResource( "data/images/missing.png" ), new File( getDataFolder() + "/" + "missing.png" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.13.2.yml" ), new File( PALETTE_DIR + "/" + "palette-1.13.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.11.2.yml" ), new File( PALETTE_DIR + "/" + "palette-1.11.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.12.2.yml" ), new File( PALETTE_DIR + "/" + "palette-1.12.2.yml" ), false );
			FileUtil.saveToFile( getResource( "data/palettes/palette-1.15.1.yml" ), new File( PALETTE_DIR + "/" + "palette-1.12.2.yml" ), false );
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
					
					mapManager.convert( handler.getUtil().getMap( mapId ), map );
				}
			}
		}
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder() + "/" + "config.yml" ) );
		for ( String string : config.getStringList( "skip-ids" ) ) {
			invalidIds.add( Integer.valueOf( string ) );
		}
		forceLoad = config.getBoolean( "force-load" );
		rotateByDefault = config.getBoolean( "rotate-by-default", true );
		
		for ( String string : config.getStringList( "blacklisted-inventories" ) ) {
			try {
				InventoryType invalid = InventoryType.valueOf( string );
				invalidInventoryTypes.add( invalid );
				getLogger().info( "Added '" + string + "' as a blacklisted inventory" );
			} catch ( IllegalArgumentException exception ) {
				getLogger().warning( "No such inventory type '" + string + "'" );
			}
		}
	}
	
	private void loadPalettes() {
		getLogger().info( "Constructing vanilla palette..." );
		MinimapPalette vanilla = handler.getVanillaPalette();
		paletteManager.register( "default", vanilla );
		
		File vanillaPalette = new File( PALETTE_DIR + "/" + "vanilla.yml" );
		if ( !vanillaPalette.exists() ) {
			getLogger().info( "Vanilla palette not found, saving..." );
			
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
		
		getLogger().info( ( vanilla.getMaterials().size() + vanilla.getTransparentBlocks().size() ) + " materials mapped for vanilla" );
		
		getLogger().info( "Loading local palette files..." );
		if ( PALETTE_DIR.exists() ) {
			for ( File file : PALETTE_DIR.listFiles() ) {
				if ( !file.isDirectory() ) {
					FileConfiguration configuration = YamlConfiguration.loadConfiguration( file );
					MinimapPalette palette = paletteManager.load( configuration );
					
					String id = file.getName().replaceAll( "\\.yml$", "" );
					paletteManager.register( id, palette );
					
					getLogger().info( "Loaded palette '" + id + "' successfully!" );
				}
			}
		} else {
			getLogger().warning( "Palette folder not discovered!" );
		}
	}
	
	private void loadImages() {
		OVERLAY_IMAGE = getImageFile( getDataFolder(), "overlay" );
		BACKGROUND_IMAGE = getImageFile( getDataFolder(), "background" );
		MISSING_MAP_IMAGE = getImageFile( getDataFolder(), "missing" );
		
		try {
			if ( OVERLAY_IMAGE.exists() ) {
				getLogger().info( "Overlay detected!" );
				this.overlay = new SimpleImage( OVERLAY_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				getLogger().warning( "Overlay image does not exist!" );
			}

			if ( BACKGROUND_IMAGE.exists() ) {
				getLogger().info( "Background detected!" );
				this.loadingBackground = new SimpleImage( BACKGROUND_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				getLogger().warning( "Background image does not exist!" );
			}
			
			if ( MISSING_MAP_IMAGE.exists() ) {
				getLogger().info( "Missing map image detected!" );
				missingMapImage = new SimpleImage( MISSING_MAP_IMAGE, 128, 128, Image.SCALE_REPLICATE );
			} else {
				getLogger().warning( "Missing map image does not exist!" );
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
	
	public MinimapManager getMapManager() {
		return mapManager;
	}
	
	public PaletteManager getPaletteManager() {
		return paletteManager;
	}
	
	public ModuleManager getModuleManager() {
		return moduleManager;
	}
	
	protected Map< Integer, CartographerRenderer > getRenderers() {
		return renderers;
	}
	
	protected Set< Integer > getInvalidIds() {
		return invalidIds;
	}
	
	public int getChunksPerSecond() {
		return chunksPerSecond;
	}
	
	public boolean isForceLoad() {
		return forceLoad;
	}
	
	public boolean isRotateByDefault() {
		return rotateByDefault;
	}
	
	public SimpleImage getLoadingImage() {
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
	
	public boolean isValidInventory( InventoryType type ) {
		return !invalidInventoryTypes.contains( type );
	}
	
	private static File getImageFile( File dir, String prefix ) {
		File image = new File( dir + "/" + prefix + ".png" );
		for ( File file : dir.listFiles() ) {
			String fileName = file.getName();
			if ( fileName.equalsIgnoreCase( prefix + ".gif" ) ) {
				return file;
			} else if ( fileName.equalsIgnoreCase( prefix + ".png" ) || fileName.equalsIgnoreCase( prefix + ".jpg" ) || fileName.equalsIgnoreCase( prefix + ".jpeg" ) || fileName.equalsIgnoreCase( prefix + ".bmp" ) ) {
				image = file;
			}
		}
		return image;
	}
	
	public static Cartographer getInstance() {
		return INSTANCE;
	}
	
	public static GeneralUtil getUtil() {
		return getInstance().getHandler().getUtil();
	}
}
