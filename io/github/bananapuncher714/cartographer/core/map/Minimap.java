package io.github.bananapuncher714.cartographer.core.map;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkLoadedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkProcessedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.MapUpdateBlockEvent;
import io.github.bananapuncher714.cartographer.core.api.map.LocalCursorProvider;
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

public class Minimap implements ChunkNotifier {
	protected final String id;
	
	protected MinimapPalette palette;
	protected MapDataCache cache;
	protected BigChunkQueue queue;;
	protected File saveFile;
	protected MapSettings settings;
	
	protected Set< WorldCursorProvider > cursorProviders = new HashSet< WorldCursorProvider >();
	protected Set< LocalCursorProvider > localCursorProviders = new HashSet< LocalCursorProvider >();
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
		
		cache.setNotifier( this );
		
		// TEST
		registerWorldCursorProvider( new WorldCursorProvider() {
			@Override
			public Collection< RealWorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
				Set< RealWorldCursor > cursors = new HashSet< RealWorldCursor >();
				cursors.add( new RealWorldCursor( player.getName(), player.getLocation(), Type.WHITE_POINTER, true ) );
				return cursors;
			}
		} );
		
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
	
	public Collection< RealWorldCursor > getCursorsFor( Player player, PlayerSetting setting ) {
		Set< RealWorldCursor > cursors = new HashSet< RealWorldCursor >();
		for ( WorldCursorProvider provider : cursorProviders ) {
			Collection< RealWorldCursor > cursorCollection = provider.getCursors( player, this, setting );
			if ( cursorCollection != null ) {
				cursors.addAll( cursorCollection );
			}
		}
		return cursors;
	}
	
	public Collection< MapCursor > getLocalCursorsFor( Player player, PlayerSetting setting ) {
		Set< MapCursor > cursors = new HashSet< MapCursor >();
		for ( LocalCursorProvider provider : localCursorProviders ) {
			Collection< MapCursor > cursorCollection = provider.getCursors( player, this, setting );
			if ( cursorCollection != null ) {
				cursors.addAll( cursorCollection );
			}
		}
		return cursors;
	}
	
	public void registerWorldPixelProvider( WorldPixelProvider provider ) {
		worldPixelProviders.add( provider );
	}
	
	public void unregisterWorldPixelProvider( WorldPixelProvider provider ) {
		worldPixelProviders.remove( provider );
	}
	
	public void registerPixelProvider( MapPixelProvider provider ) {
		pixelProviders.add( provider );
	}
	
	public void unregisterPixelProvider( MapPixelProvider provider ) {
		pixelProviders.remove( provider );
	}
	
	public void registerWorldCursorProvider( WorldCursorProvider provider ) {
		cursorProviders.add( provider );
	}
	
	public void unregisterWorldCursorProvider( WorldCursorProvider provider ) {
		cursorProviders.remove( provider );
	}
	
	public void registerLocalCursorProvider( LocalCursorProvider provider ) {
		localCursorProviders.add( provider );
	}
	
	public void unregisterLocalCursorProvider( LocalCursorProvider provider ) {
		localCursorProviders.remove( provider );
	}
	
	public Set< WorldCursorProvider > getCursorProviders() {
		return cursorProviders;
	}
	
	public Set< LocalCursorProvider > getLocalCursorProviders() {
		return localCursorProviders;
	}
	
	public Set< MapPixelProvider > getPixelProviders() {
		return pixelProviders;
	}
	
	public Set< WorldPixelProvider > getWorldPixelProviders() {
		return worldPixelProviders;
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
