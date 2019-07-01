package io.github.bananapuncher714.cartographer.core.map;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkLoadedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkProcessedEvent;
import io.github.bananapuncher714.cartographer.core.api.map.MapPixelProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.renderer.MapViewer;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;

public class Minimap implements ChunkNotifier {
	protected final String id;
	
	protected MinimapPalette palette;
	protected MapDataCache cache;
	protected BigChunkQueue queue;;
	protected File saveFile;
	protected MapSettings settings;
	
	protected Map< UUID, MapViewer > viewers = new HashMap< UUID, MapViewer >();

	protected Set< WorldCursorProvider > cursorProviders = new HashSet< WorldCursorProvider >();
	protected Set< MapPixelProvider > pixelProviders = new HashSet< MapPixelProvider >();
	
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
			public Collection<RealWorldCursor> getCursors( Player player, Minimap map ) {
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
//		pixelProviders.add( new MapPixelProvider() {
//			@Override
//			public Collection< MapPixel > getMapPixels( Player player, Minimap map ) {
//				Set< MapPixel > pixels = new HashSet< MapPixel >();
//				for ( int x = 0; x < 64; x++ ) {
//					for ( int y = 0; y < 64; y++ ) {
//						pixels.add( new MapPixel( x, y, new Color( 0, 0, 0, 128 ) ) );
//					}
//				}
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
				if ( needsRender( location ) ) {
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
	
	public Collection< MapPixel > getPixelsFor( Player player ) {
		Set< MapPixel > pixels = new HashSet< MapPixel >();
		for ( MapPixelProvider provider : pixelProviders ) {
			Collection< MapPixel > cursorCollection = provider.getMapPixels( player, this );
			if ( cursorCollection != null ) {
				pixels.addAll( cursorCollection );
			}
		}
		return pixels;
	}
	
	public Collection< RealWorldCursor > getCursorsFor( Player player ) {
		Set< RealWorldCursor > cursors = new HashSet< RealWorldCursor >();
		for ( WorldCursorProvider provider : cursorProviders ) {
			Collection< RealWorldCursor > cursorCollection = provider.getCursors( player, this );
			if ( cursorCollection != null ) {
				cursors.addAll( cursorCollection );
			}
		}
		return cursors;
	}
	
	public void registerWorldCursorProvider( WorldCursorProvider provider ) {
		cursorProviders.add( provider );
	}
	
	public void unregisterWorldCursorProvider( WorldCursorProvider provider ) {
		cursorProviders.remove( provider );
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
