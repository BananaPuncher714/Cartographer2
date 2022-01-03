package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class SimplerChunkDataStorage implements ChunkDataStorage {
	protected Map< String, WorldStorage > caches;
	protected Set< ChunkLocation > flatStorage;
	
	public SimplerChunkDataStorage() {
		caches = new ConcurrentHashMap< String, WorldStorage >();
		flatStorage = Collections.newSetFromMap( new ConcurrentHashMap< ChunkLocation, Boolean >() );
	}
	
	@Override
	public void store( ChunkLocation location, ChunkData data ) {
		flatStorage.add( location );
		
		WorldStorage cache = caches.get( location.getWorldName() );
		if ( cache == null ) {
			cache = new WorldStorage();
			caches.put( location.getWorldName(), cache );
		}
		
		cache.store( location.getX(), location.getZ(), data );
	}

	@Override
	public void remove( ChunkLocation location ) {
		flatStorage.remove( location );

		WorldStorage cache = caches.get( location.getWorldName() );
		if ( cache != null ) {
			cache.remove( location.getX(), location.getZ() );
		}
	}

	@Override
	public ChunkData get( ChunkLocation location ) {
		WorldStorage cache = caches.get( location.getWorldName() );
		if ( cache != null ) {
			return cache.get( location.getX(), location.getZ() );
		}
		return null;
	}
	
	public ChunkData get( String world, int x, int y ) {
		WorldStorage cache = caches.get( world );
		if ( cache != null ) {
			return cache.get( x, y );
		}
		return null;
	}

	@Override
	public Collection< ChunkLocation > getLocations() {
		return flatStorage;
	}

	@Override
	public boolean contains( ChunkLocation location ) {
		return flatStorage.contains( location );
	}

	@Override
	public byte getColorAt( Location location ) {
		int blockX = location.getBlockX();
		int blockZ = location.getBlockZ();
		int chunkX = blockX >> 4;
		int chunkZ = blockZ >> 4;
		
		ChunkLocation cLocation = new ChunkLocation( location.getWorld(), chunkX, chunkZ );
		int xOffset = blockX & 0xF;
		int zOffset = blockZ & 0xF;
		
		ChunkData data = get( cLocation );
		return data == null ? -1 : data.getDataAt( xOffset, zOffset );
	}

	@Override
	public byte getColorAt( Location location, double scale ) {
		int blockX = location.getBlockX();
		int blockZ = location.getBlockZ();
		int chunkX = blockX >> 4;
		int chunkZ = blockZ >> 4;
		
		ChunkLocation cLocation = new ChunkLocation( location.getWorld(), chunkX, chunkZ );
		int xOffset = blockX & 0xF;
		int zOffset = blockZ & 0xF;
		
		ChunkData data = get( cLocation );
		return data == null ? -1 : data.getDataAt( xOffset, zOffset, scale );
	}
	
	private class WorldStorage {
		Map< Long, ChunkData > data;
		
		WorldStorage() {
			data = new ConcurrentHashMap< Long, ChunkData >();
		}
		
		void store( int x, int y, ChunkData data ) {
			this.data.put( ( 0L | x ) << 32 | y, data );
		}
		
		void remove( int x, int y ) {
			data.remove( ( 0L | x ) << 32 | y );
		}
		
		ChunkData get( int x, int y ) {
			return data.get( ( 0L | x ) << 32 | y );
		}
	}
}
