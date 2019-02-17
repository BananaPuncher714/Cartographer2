package io.github.bananapuncher714.cartographer.core.map;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkLoadedEvent;
import io.github.bananapuncher714.cartographer.core.api.events.ChunkProcessedEvent;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.MapDataCache.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;

public class Minimap implements ChunkNotifier {
	protected final String id;
	
	protected MinimapPalette palette;
	protected MapDataCache cache;
	protected BigChunkQueue queue;;
	protected File saveFile;
	protected MapSettings settings;
	
	private long tick = 0;
	
	public Minimap( String id, MinimapPalette palette, MapDataCache cache, File saveDir, MapSettings settings ) {
		this.id = id;
		this.saveFile = saveDir;
		this.palette = palette;
		this.cache = cache;
		this.queue = new BigChunkQueue( new File( saveDir + "/" + "cache" ), cache );
		this.settings = settings;
		
		cache.setNotifier( this );
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
