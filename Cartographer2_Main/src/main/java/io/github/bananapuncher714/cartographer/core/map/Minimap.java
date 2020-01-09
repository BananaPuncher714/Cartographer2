package io.github.bananapuncher714.cartographer.core.map;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.events.chunk.ChunkLoadedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.chunk.ChunkProcessedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MapUpdateBlockEvent;
import io.github.bananapuncher714.cartographer.core.api.map.MapCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.MapPixelProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class Minimap implements ChunkNotifier {
	protected final String id;
	
	protected final File OVERLAY_IMAGE_FILE;
	protected final File BACKGROUND_IMAGE_FILE;
	
	protected MinimapPalette palette;
	protected MapDataCache cache;
	protected BigChunkQueue queue;;
	protected File saveFile;
	protected MapSettings settings;

	// Local images
	protected SimpleImage overlay;
	protected SimpleImage background;
	
	protected Set< WorldCursorProvider > cursorProviders = new HashSet< WorldCursorProvider >();
	protected Set< MapCursorProvider > localCursorProviders = new HashSet< MapCursorProvider >();
	protected Set< MapPixelProvider > pixelProviders = new HashSet< MapPixelProvider >();
	protected Set< WorldPixelProvider > worldPixelProviders = new HashSet< WorldPixelProvider >();
	
	private long tick = 0;
	
	public Minimap( String id, MinimapPalette palette, MapDataCache cache, File saveDir, MapSettings settings ) {
		this.id = id;
		this.saveFile = saveDir;
		this.palette = palette;
		this.cache = cache;
		this.queue = new BigChunkQueue( new File( saveDir + "/" + "cache" ), cache );
		this.settings = settings;
		
		OVERLAY_IMAGE_FILE = FileUtil.getImageFile( saveDir, "overlay" );
		BACKGROUND_IMAGE_FILE = FileUtil.getImageFile( saveDir, "background" );
		
		cache.setNotifier( this );
		
		load();
		
		// Show at least the player, if nothing else
		registerProvider( new DefaultPlayerCursorProvider() );
		registerProvider( new DefaultPointerCursorProvider() );
		
//		registerWorldCursorProvider( new WorldCursorProvider() {
//			@Override
//			public Collection< RealWorldCursor > getCursors( Player player, Minimap map ) {
//				Set< RealWorldCursor > cursors = new HashSet< RealWorldCursor >();
//				for ( Entity entity : player.getNearbyEntities( 20, 10, 20 ) ) {
//					cursors.add( new RealWorldCursor( entity.getName(), entity.getLocation(), Type.BLUE_POINTER, true ) );
//				}
//				return cursors;
//			}
//		} );
//		
//		Font font = new JLabel().getFont();
//		CartographerFont cFont = new CartographerFont( font );
//		BufferedImage image = cFont.write( "Welcome to Cartographer", Color.BLACK, 12 );
//		Collection< MapPixel > pixels = MapUtil.getPixelsFor( image, 0, 64 );
//		
//		pixelProviders.add( new MapPixelProvider() {
//			@Override
//			public Collection< MapPixel > getMapPixels( Player player, Minimap map ) {
//				return pixels;
//			}
//		} );
	}
	
	protected void load() {
		try {
			if ( OVERLAY_IMAGE_FILE.exists() ) {
				overlay = new SimpleImage( OVERLAY_IMAGE_FILE, 128, 128, Image.SCALE_REPLICATE );
				Cartographer.getInstance().getLogger().info( "[Minimap] [" + id + "] Loaded overlay image '" + OVERLAY_IMAGE_FILE + "'" );
			}
			
			if ( BACKGROUND_IMAGE_FILE.exists() ) {
				background = new SimpleImage( BACKGROUND_IMAGE_FILE, 128, 128, Image.SCALE_REPLICATE );
				Cartographer.getInstance().getLogger().info( "[Minimap] [" + id + "] Loaded background image '" + BACKGROUND_IMAGE_FILE + "'" );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	public String getId() {
		return id;
	}
	
	public void update() {
		cache.update();
		queue.update();
		if ( tick++ % 100 == 0 ) {
			Set< BigChunkLocation > noSave = new HashSet< BigChunkLocation >();
			Map< BigChunkLocation, BigChunk > chunks = new HashMap< BigChunkLocation, BigChunk >();
			for ( Entry< ChunkLocation, ChunkData > entry : cache.getData().entrySet() ) {
				ChunkLocation location = entry.getKey();
				BigChunkLocation bigLoc = new BigChunkLocation( location );
				if ( noSave.contains( bigLoc ) ) {
					continue;
				}
				if ( needsRender( location ) || location.isLoaded() ) {
					noSave.add( bigLoc );
					if ( chunks.containsKey( bigLoc ) ) {
						chunks.remove( bigLoc );
					}
					continue;
				}
				
				BigChunk chunk = chunks.containsKey( bigLoc ) ? chunks.get( bigLoc ) : new BigChunk( location );
				chunk.set( location, entry.getValue() );
				chunks.put( bigLoc, chunk );
			}
			
			for ( BigChunkLocation loc : chunks.keySet() ) {
				BigChunk chunk = chunks.get( loc );
				if ( queue.save( loc, chunk ) ) {
					for ( int x = 0; x < 16; x++ ) {
						for ( int z = 0; z < 16; z++ ) {
							ChunkLocation location = new ChunkLocation( loc.getWorld(), ( loc.getX() << 4 ) + x, ( loc.getZ() << 4 ) + z );
							cache.getData().remove( location );
						}
					}
				}
			}
		}
	}
	
	public boolean needsRender( ChunkLocation location ) {
		int cx = location.getX() >> 4 << 8;
		int cz = location.getZ() >> 4 << 8;
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			Location playerLoc = player.getLocation();
			if ( playerLoc.getWorld() != location.getWorld() ) {
				continue;
			}
			int x = playerLoc.getBlockX();
			int z = playerLoc.getBlockZ();
			if ( BlockUtil.distance( cx, cz, x, z ) < 1800 ) {
				return true;
			}
		}
		return false;
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}
	
	public MapDataCache getDataCache() {
		return cache;
	}
	
	public BigChunkQueue getQueue() {
		return queue;
	}
	
	public MapSettings getSettings() {
		return settings;
	}
	
	public SimpleImage getOverlayImage() {
		return overlay == null ? Cartographer.getInstance().getOverlay() : overlay;
	}
	
	public void setOverlayImage( SimpleImage image ) {
		if ( image != null ) {
			overlay = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
		}
	}
	
	public SimpleImage getBackgroundImage() {
		return background == null ? Cartographer.getInstance().getBackground() : background;
	}
	
	public void setBackgroundImage( SimpleImage image ) {
		if ( image != null ) {
			background = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
		}
	}
	
	public File getDataFolder() {
		return saveFile;
	}

	public void setSettings( MapSettings settings ) {
		this.settings = settings;
	}
	
	public Collection< MapPixel > getPixelsFor( Player player, PlayerSetting setting ) {
		Set< MapPixel > pixels = new TreeSet< MapPixel >();
		for ( MapPixelProvider provider : pixelProviders ) {
			Collection< MapPixel > cursorCollection = provider.getMapPixels( player, this, setting );
			if ( cursorCollection != null ) {
				pixels.addAll( cursorCollection );
			}
		}
		return pixels;
	}
	
	public Collection< WorldPixel > getWorldPixelsFor( Player player, PlayerSetting setting ) {
		Set< WorldPixel > pixels = new TreeSet< WorldPixel >();
		for ( WorldPixelProvider provider : worldPixelProviders ) {
			Collection< WorldPixel > cursorCollection = provider.getWorldPixels( player, this, setting );
			if ( cursorCollection != null ) {
				pixels.addAll( cursorCollection );
			}
		}
		return pixels;
	}
	
	public Collection< WorldCursor > getCursorsFor( Player player, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();
		for ( WorldCursorProvider provider : cursorProviders ) {
			Collection< WorldCursor > cursorCollection = provider.getCursors( player, this, setting );
			if ( cursorCollection != null ) {
				cursors.addAll( cursorCollection );
			}
		}
		return cursors;
	}
	
	public Collection< MapCursor > getLocalCursorsFor( Player player, PlayerSetting setting ) {
		Set< MapCursor > cursors = new HashSet< MapCursor >();
		for ( MapCursorProvider provider : localCursorProviders ) {
			Collection< MapCursor > cursorCollection = provider.getCursors( player, this, setting );
			if ( cursorCollection != null ) {
				cursors.addAll( cursorCollection );
			}
		}
		return cursors;
	}
	
	public void registerProvider( WorldPixelProvider provider ) {
		worldPixelProviders.add( provider );
	}
	
	public void unregisterProvider( WorldPixelProvider provider ) {
		worldPixelProviders.remove( provider );
	}
	
	public void registerProvider( MapPixelProvider provider ) {
		pixelProviders.add( provider );
	}
	
	public void unregisterProvider( MapPixelProvider provider ) {
		pixelProviders.remove( provider );
	}
	
	public void registerProvider( WorldCursorProvider provider ) {
		cursorProviders.add( provider );
	}
	
	public void unregisterProvider( WorldCursorProvider provider ) {
		cursorProviders.remove( provider );
	}
	
	public void registerProvider( MapCursorProvider provider ) {
		localCursorProviders.add( provider );
	}
	
	public void unregisterProvider( MapCursorProvider provider ) {
		localCursorProviders.remove( provider );
	}
	
	public Set< WorldCursorProvider > getWorldCursorProviders() {
		return cursorProviders;
	}
	
	public Set< MapCursorProvider > getMapCursorProviders() {
		return localCursorProviders;
	}
	
	public Set< MapPixelProvider > getMapPixelProviders() {
		return pixelProviders;
	}
	
	public Set< WorldPixelProvider > getWorldPixelProviders() {
		return worldPixelProviders;
	}
	
	/**
	 * Set the {@link MinimapPalette} for this minimap.
	 * 
	 * @param palette
	 * Cannot be null.
	 */
	public void setPalette( MinimapPalette palette ) {
		Validate.notNull( palette );
	}
	
	public void updateLocation( Location location ) {
		MapUpdateBlockEvent event = new MapUpdateBlockEvent( this, location, palette );
		event.callEvent();
		
		cache.updateLocation( location, event.getPalette() );
	}
	
	public void terminate() {
		cache.terminate();
		
		Map< BigChunkLocation, BigChunk > chunks = new HashMap< BigChunkLocation, BigChunk >();
		for ( Entry< ChunkLocation, ChunkData > entry : cache.getData().entrySet() ) {
			ChunkLocation location = entry.getKey();
			BigChunkLocation bigLoc = new BigChunkLocation( location );
			BigChunk chunk = chunks.containsKey( bigLoc ) ? chunks.get( bigLoc ) : new BigChunk( location );
			chunk.set( location, entry.getValue() );
			chunks.put( bigLoc, chunk );
		}
		
		for ( BigChunkLocation loc : chunks.keySet() ) {
			BigChunk chunk = chunks.get( loc );
			queue.save( loc, chunk );
		}
		queue.saveBlocking();
	}
	
	@Override
	public ChunkData onChunkLoad( ChunkLocation location, ChunkData data ) {
		ChunkLoadedEvent event = new ChunkLoadedEvent( this, location, data );
		
		Bukkit.getPluginManager().callEvent( event );
		
		return event.getData();
	}

	@Override
	public ChunkData onChunkProcessed( ChunkLocation location, ChunkData data ) {
		ChunkProcessedEvent event = new ChunkProcessedEvent( this, location, data );
		
		Bukkit.getPluginManager().callEvent( event );
		
		return event.getData();
	}
}
