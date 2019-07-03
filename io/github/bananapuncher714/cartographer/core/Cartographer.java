package io.github.bananapuncher714.cartographer.core;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.ReflectionUtil;
import io.github.bananapuncher714.cartographer.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;

public class Cartographer extends JavaPlugin implements Listener {
	private static Cartographer INSTANCE;
	
	private static File PALETTE_DIR;
	private static File MAP_DIR;
	
	private static File CONFIG_FILE;
	private static File DATA_FILE;
	
	private static File MISSING_MAP_IMAGE;
	private static File OVERLAY_IMAGE;
	private static File BACKGROUND_IMAGE;
	
	private TinyProtocol protocol;
	private PacketHandler handler;
	
	private MinimapManager mapManager;
	private PaletteManager paletteManager;
	
	private Set< Integer > invalidIds = new HashSet< Integer >();
	
	private Map< Integer, CartographerRenderer > renderers = new HashMap< Integer, CartographerRenderer >();
	
	private CartographerCommand command;
	
	private int chunksPerSecond = 1;
	private boolean forceLoad = false;
	private boolean rotateByDefault = true;
	
	private int[] loadingBackground;
	private int[] overlay;
	private byte[] missingMapImage;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		PALETTE_DIR = new File( getDataFolder() + "/" + "palettes/" );
		MAP_DIR = new File( getDataFolder() + "/" + "maps/" );
		
		CONFIG_FILE = new File( getDataFolder() + "/" + "config.yml" );
		DATA_FILE = new File( getDataFolder() + "/" + "data.yml" );
		
		OVERLAY_IMAGE = new File( getDataFolder() + "/" + "overlay.png" );
		BACKGROUND_IMAGE = new File( getDataFolder() + "/" + "background.png" );
		MISSING_MAP_IMAGE = new File( getDataFolder() + "/" + "missing.png" );
		
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
		
		command = new CartographerCommand();
		getCommand( "cartographer" ).setExecutor( command );
		getCommand( "cartographer" ).setTabCompleter( command );
		
		Bukkit.getScheduler().runTaskTimer( this, this::update, 5, 1 );
		Bukkit.getScheduler().runTaskTimer( this, ChunkLoadListener.INSTANCE::update, 5, 10 );
		
		Bukkit.getPluginManager().registerEvents( new PlayerListener( this ), this );
//		Bukkit.getPluginManager().registerEvents( new MapListener(), this );
		Bukkit.getPluginManager().registerEvents( ChunkLoadListener.INSTANCE, this );
		
		load();
	}
	
	@Override
	public void onDisable() {
		for ( CartographerRenderer renderer : renderers.values() ) {
			renderer.terminate();
		}
		mapManager.terminate();
		saveData();
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
		loadConfig();
		loadImages();
		
		// Load the palettes
		loadPalettes();
		
		// Load the maps
		// Requires palettes
		loadMaps();
		
		// Load the data
		// Requires maps
		loadData();
	}
	
	private void loadInit() {
		FileUtil.saveToFile( getResource( "config.yml" ), CONFIG_FILE, false );
		FileUtil.saveToFile( getResource( "data/images/overlay.png" ), OVERLAY_IMAGE, false );
		FileUtil.saveToFile( getResource( "data/images/background.png" ), BACKGROUND_IMAGE, false );
		FileUtil.saveToFile( getResource( "data/images/missing.png" ), MISSING_MAP_IMAGE, false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.13.2.yml" ), new File( PALETTE_DIR + "/" + "palette-1.13.2.yml" ), false );
		FileUtil.saveToFile( getResource( "data/palettes/palette-1.11.2.yml" ), new File( PALETTE_DIR + "/" + "palette-1.11.2.yml" ), false );

	}
	
	private void loadData() {
		if ( DATA_FILE.exists() ) {
			FileConfiguration data = YamlConfiguration.loadConfiguration( DATA_FILE );
			if ( data.contains( "custom-renderer-ids" ) ) {
				for ( String key : data.getConfigurationSection( "custom-renderer-ids" ).getKeys( false ) ) {
					String id = data.getString( "custom-renderer-ids." + key );
					short mapId = Short.parseShort( key );
					Minimap map = mapManager.getMinimaps().get( id );
					
					mapManager.convert( Bukkit.getMap( mapId ), map );
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
	}
	
	private void loadPalettes() {
		if ( PALETTE_DIR.exists() ) {
			for ( File file : PALETTE_DIR.listFiles() ) {
				FileConfiguration configuration = YamlConfiguration.loadConfiguration( file );
				MinimapPalette palette = paletteManager.load( configuration );
				
				String id = file.getName().replaceAll( "\\.yml$", "" );
				paletteManager.register( id, palette );
				
				getLogger().info( "Loaded palette '" + id + "' successfully!" );
			}
		} else {
			getLogger().warning( "Palette folder not discovered!" );
		}
	}
	
	private void loadImages() {
		try {
			if ( OVERLAY_IMAGE.exists() ) {
				getLogger().info( "Overlay detected!" );
				this.overlay = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( ImageIO.read( OVERLAY_IMAGE ).getScaledInstance( 128, 128, Image.SCALE_REPLICATE ) ) );
			}

			if ( BACKGROUND_IMAGE.exists() ) {
				getLogger().info( "Background detected!" );
				this.loadingBackground = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( ImageIO.read( BACKGROUND_IMAGE ).getScaledInstance( 128, 128, Image.SCALE_REPLICATE ) ) );
			}
			if ( MISSING_MAP_IMAGE.exists() ) {
				getLogger().info( "Missing map image detected!" );
				missingMapImage = JetpImageUtil.dither( ImageIO.read( MISSING_MAP_IMAGE ).getScaledInstance( 128, 128, Image.SCALE_REPLICATE ) );
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
	
	public File getAndConstructMapDir( String id ) {
		File dir = new File( MAP_DIR + "/" + id );
		FileUtil.saveToFile( getResource( "data/minimap-config.yml" ), new File( dir + "/" + "config.yml" ), false );
		
		return dir;
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
	
	public int[] getLoadingImage() {
		// TODO Specify that this is 128x128
		return loadingBackground;
	}
	
	public int[] getOverlay() {
		// TODO Specify that this is 128x128
		return overlay;
	}

	public byte[] getMissingMapImage() {
		return missingMapImage;
	}
	
	public static Cartographer getInstance() {
		return INSTANCE;
	}
	
	public static GeneralUtil getUtil() {
		return getInstance().getHandler().getUtil();
	}
}
