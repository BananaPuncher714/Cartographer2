package io.github.bananapuncher714.cartographer.core.map;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.events.chunk.ChunkLoadedEvent;
import io.github.bananapuncher714.cartographer.core.api.map.MapCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.MapPixelProvider;
import io.github.bananapuncher714.cartographer.core.api.map.MapProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class Minimap implements ChunkNotifier {
	protected final String id = null;
	
	protected final File OVERLAY_IMAGE_FILE = null;
	protected final File BACKGROUND_IMAGE_FILE = null;
	protected final File DISABLED_IMAGE_FILE = null;
	
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

	public Minimap( String id, MinimapPalette palette, MapDataCache cache, File saveDir, MapSettings settings ) {
	}
	
	protected void load() {
	}
	
	public String getId() {
		return id;
	}
	
	public void update() {
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
		return null;
	}
	
	public void setOverlayImage( SimpleImage image ) {
	}
	
	public SimpleImage getBackgroundImage() {
		return null;
	}
	
	public void setBackgroundImage( SimpleImage image ) {
	}
	
	public SimpleImage getDisabledImage() {
		return null;
	}
	
	public void setDisabledImage( SimpleImage image ) {
	}

	public File getDataFolder() {
		return saveFile;
	}

	public void setSettings( MapSettings settings ) {
		this.settings = settings;
	}
	
	public Collection< MapPixel > getPixelsFor( Player player, PlayerSetting setting ) {
		return null;
	}
	
	public Collection< WorldPixel > getWorldPixelsFor( Player player, PlayerSetting setting ) {
		return null;
	}
	
	public Collection< WorldCursor > getCursorsFor( Player player, PlayerSetting setting ) {
		return null;
	}
	
	public Collection< MapCursor > getLocalCursorsFor( Player player, PlayerSetting setting ) {
		return null;
	}
	
	public void register( MapProvider provider ) {
	}
	
	public void unregister( MapProvider provider ) {
	}
	
	public void registerProvider( WorldPixelProvider provider ) {
	}
	
	public void unregisterProvider( WorldPixelProvider provider ) {
	}
	
	public void registerProvider( MapPixelProvider provider ) {
	}
	
	public void unregisterProvider( MapPixelProvider provider ) {
	}
	
	public void registerProvider( WorldCursorProvider provider ) {
	}
	
	public void unregisterProvider( WorldCursorProvider provider ) {
	}
	
	public void registerProvider( MapCursorProvider provider ) {
	}
	
	public void unregisterProvider( MapCursorProvider provider ) {
	}
	
	public Set< WorldCursorProvider > getWorldCursorProviders() {
		return null;
	}
	
	public Set< MapCursorProvider > getMapCursorProviders() {
		return null;
	}
	
	public Set< MapPixelProvider > getMapPixelProviders() {
		return null;
	}
	
	public Set< WorldPixelProvider > getWorldPixelProviders() {
		return null;
	}
	
	/**
	 * Set the {@link MinimapPalette} for this minimap.
	 * 
	 * @param palette
	 * Cannot be null.
	 */
	public void setPalette( MinimapPalette palette ) {
	}
	
	public void updateLocation( Location location ) {
	}
	
	public void terminate() {
	}
	
	@Override
	public ChunkData onChunkLoad( ChunkLocation location, ChunkData data ) {
		ChunkLoadedEvent event = new ChunkLoadedEvent( this, location, data );
		
		Bukkit.getPluginManager().callEvent( event );
		
		return event.getData();
	}

	@Override
	public ChunkData onChunkProcessed( ChunkLocation location, ChunkData data ) {
		return null;
	}
	
	private class MinimapLogger extends Logger {
		protected MinimapLogger( Minimap map ) {
			super( map.getId(), null );
		}

		@Override
		public void log( LogRecord record ) {
		}
		
		public void infoTr( String key, Object... params ) {
		}
		
		public void warningTr( String key, Object... params ) {
		}
		
		public void severeTr( String key, Object... params ) {
		}
	}
}
