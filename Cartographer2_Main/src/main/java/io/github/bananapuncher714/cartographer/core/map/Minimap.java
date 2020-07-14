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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import io.github.bananapuncher714.cartographer.core.api.map.MapProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class Minimap implements ChunkNotifier {
	protected final String id;
	
	protected final File OVERLAY_IMAGE_FILE;
	protected final File BACKGROUND_IMAGE_FILE;
	protected final File DISABLED_IMAGE_FILE;
	
	protected MinimapPalette palette;
	protected MapDataCache cache;
	protected BigChunkQueue queue;;
	protected File saveFile;
	protected MapSettings settings;

	// Local images
	protected SimpleImage overlay;
	protected SimpleImage background;
	protected SimpleImage disabled;
	
	protected Set< WorldCursorProvider > cursorProviders = new HashSet< WorldCursorProvider >();
	protected Set< MapCursorProvider > localCursorProviders = new HashSet< MapCursorProvider >();
	protected Set< MapPixelProvider > pixelProviders = new HashSet< MapPixelProvider >();
	protected Set< WorldPixelProvider > worldPixelProviders = new HashSet< WorldPixelProvider >();
	
	protected MinimapLogger logger;

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
		DISABLED_IMAGE_FILE = FileUtil.getImageFile( saveDir, "disabled" );
		
		cache.setNotifier( this );
		
		logger = new MinimapLogger( this );
		
		load();
		
		logger.infoTr( LocaleConstants.MINIMAP_DEFAULT_ROTATION, settings.getRotation() );
		logger.infoTr( LocaleConstants.MINIMAP_AUTO_UPDATE, settings.isAutoUpdate() );
		logger.infoTr( LocaleConstants.MINIMAP_ZOOM_CIRCULAR, settings.isCircularZoom() );
		logger.infoTr( LocaleConstants.MINIMAP_RENDER, settings.isRenderOutOfBorder() );
		logger.infoTr( LocaleConstants.MINIMAP_ZOOM_DEFAULT, settings.getDefaultZoom() );
		logger.infoTr( LocaleConstants.MINIMAP_ZOOM_ALLOWED, String.join( ", ", settings.allowedZooms.stream()
				.map( d -> { return String.valueOf( d ); } )
				.collect( Collectors.toList() ) ) );
		
		if ( settings.isWhitelist ) {
			logger.infoTr( LocaleConstants.MINIMAP_WORLD_WHITELIST, String.join( ", ", settings.blacklistedWorlds ) );
		} else {
			logger.infoTr( LocaleConstants.MINIMAP_WORLD_BLACKLIST, String.join( ", ", settings.blacklistedWorlds ) );
		}
		
		// Show at least the player, if nothing else
		registerProvider( new DefaultPlayerCursorProvider() );
		registerProvider( new DefaultPointerCursorProvider() );
	}
	
	protected void load() {
		try {
			if ( OVERLAY_IMAGE_FILE.exists() ) {
				overlay = new SimpleImage( OVERLAY_IMAGE_FILE, 128, 128, Image.SCALE_REPLICATE );
				logger.infoTr( LocaleConstants.MINIMAP_LOADED_OVERLAY, OVERLAY_IMAGE_FILE.getName() );
			}
			
			if ( BACKGROUND_IMAGE_FILE.exists() ) {
				background = new SimpleImage( BACKGROUND_IMAGE_FILE, 128, 128, Image.SCALE_REPLICATE );
				logger.infoTr( LocaleConstants.MINIMAP_LOADED_BACKGROUND, BACKGROUND_IMAGE_FILE.getName() );
			}
			
			if ( DISABLED_IMAGE_FILE.exists() ) {
				disabled = new SimpleImage( DISABLED_IMAGE_FILE, 128, 128, Image.SCALE_REPLICATE );
				logger.infoTr( LocaleConstants.MINIMAP_LOADED_DISABLED, DISABLED_IMAGE_FILE.getName() );
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
		// Every 5 ticks, attempt to save and unload regions that are not in use
		if ( tick++ % 100 == 0 ) {
			// Set for locations that are still needed
			Set< BigChunkLocation > noSave = new HashSet< BigChunkLocation >();
			// Chunks to save
			Map< BigChunkLocation, BigChunk > chunks = new HashMap< BigChunkLocation, BigChunk >();
			for ( Entry< ChunkLocation, ChunkData > entry : cache.getData().entrySet() ) {
				ChunkLocation location = entry.getKey();
				BigChunkLocation bigLoc = new BigChunkLocation( location );
				
				// Make sure this chunk is no longer necessary
				if ( noSave.contains( bigLoc ) ) {
					continue;
				}
				
				// If not already marked as "in use", then check if it is loaded, or is waiting to be rendered
				if ( cache.withinVisiblePlayerRange( location )
						|| location.isLoaded()
						|| cache.isProcessing( location ) ) {
					// Add the location
					noSave.add( bigLoc );
					// Remove it from chunks to save if it is already queued to be saved
					if ( chunks.containsKey( bigLoc ) ) {
						chunks.remove( bigLoc );
					}
					continue;
				}
				
				// Get the big chunk that needs to be saved
				BigChunk chunk = chunks.getOrDefault( bigLoc, new BigChunk( location ) );
				
				// Add the current location and add to queue
				chunk.set( location, entry.getValue() );
				chunks.put( bigLoc, chunk );
			}
			
			for ( BigChunkLocation loc : chunks.keySet() ) {
				BigChunk chunk = chunks.get( loc );
				// Attempt to save the chunk
				// If it doesn't for some reason, then don't do anything
				if ( queue.save( loc, chunk ) ) {
					for ( int x = 0; x < 16; x++ ) {
						for ( int z = 0; z < 16; z++ ) {
							ChunkLocation location = new ChunkLocation( loc.getWorld(), ( loc.getX() << 4 ) + x, ( loc.getZ() << 4 ) + z );
							// Remove the corresponding chunk from our cache
							cache.getData().remove( location );
						}
					}
				}
			}
		}
	}
	
	public Logger getLogger() {
		return logger;
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
	
	public SimpleImage getDisabledImage() {
		return disabled;
	}
	
	public void setDisabledImage( SimpleImage image ) {
		if ( image != null ) {
			disabled = new SimpleImage( image, 218, 128, Image.SCALE_REPLICATE );
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
	
	public void register( MapProvider provider ) {
		if ( provider instanceof WorldPixelProvider ) {
			registerProvider( ( WorldPixelProvider ) provider );
		}
		if ( provider instanceof WorldCursorProvider ) {
			registerProvider( ( WorldCursorProvider ) provider );
		}
		if ( provider instanceof MapPixelProvider ) {
			registerProvider( ( MapPixelProvider ) provider );
		}
		if ( provider instanceof MapCursorProvider ) {
			registerProvider( ( MapCursorProvider ) provider );
		}
	}
	
	public void unregister( MapProvider provider ) {
		if ( provider instanceof WorldPixelProvider ) {
			unregisterProvider( ( WorldPixelProvider ) provider );
		}
		if ( provider instanceof WorldCursorProvider ) {
			unregisterProvider( ( WorldCursorProvider ) provider );
		}
		if ( provider instanceof MapPixelProvider ) {
			unregisterProvider( ( MapPixelProvider ) provider );
		}
		if ( provider instanceof MapCursorProvider ) {
			unregisterProvider( ( MapCursorProvider ) provider );
		}
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
	
	private class MinimapLogger extends Logger {
		private String format = "[%s] [Minimap] [%s] %s";
		private String mapName;
		
		protected MinimapLogger( Minimap map ) {
			super( map.getId(), null );
			mapName = map.getId();
			setParent( Cartographer.getInstance().getLogger() );
			setLevel( Level.ALL );
		}

		@Override
		public void log( LogRecord record ) {
			record.setMessage( String.format( format, Cartographer.getInstance().getName(), mapName, record.getMessage() ) );
			super.log( record );
		}
		
		public void infoTr( String key, Object... params ) {
			String message = Cartographer.getInstance().getLocaleManager().translateFor( Bukkit.getConsoleSender(), key, params );
			if ( message != null && !message.isEmpty() ) {
				info( message );
			}
		}
		
		public void warningTr( String key, Object... params ) {
			String message = Cartographer.getInstance().getLocaleManager().translateFor( Bukkit.getConsoleSender(), key, params );
			if ( message != null && !message.isEmpty() ) {
				warning( message );
			}
		}
		
		public void severeTr( String key, Object... params ) {
			String message = Cartographer.getInstance().getLocaleManager().translateFor( Bukkit.getConsoleSender(), key, params );
			if ( message != null && !message.isEmpty() ) {
				severe( message );
			}
		}
	}
}
