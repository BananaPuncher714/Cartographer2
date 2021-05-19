package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class SimpleChunkDataStorage implements ChunkDataStorage {
	protected final Map< EnormousChunkLocation, EnormousChunkMap > bigData;
	protected final Map< ChunkLocation, ChunkData > data;
	
	public SimpleChunkDataStorage() {
		data = new ConcurrentHashMap< ChunkLocation, ChunkData >();
		bigData = new ConcurrentHashMap< EnormousChunkLocation, EnormousChunkMap >();
	}
	
	@Override
	public void store( ChunkLocation location, ChunkData data ) {
		this.data.put( location, data );
		
		EnormousChunkLocation bigLocation = new EnormousChunkLocation( location );
		EnormousChunkMap map = bigData.get( bigLocation );
		if ( map == null ) {
			map = new EnormousChunkMap( bigLocation );
			bigData.put( bigLocation, map );
		}
		map.set( location, data );
	}

	@Override
	public void remove( ChunkLocation location ) {
		data.remove( location );
		EnormousChunkMap map = bigData.get( new EnormousChunkLocation( location ) );
		if ( map != null ) {
			map.set( location, null );
		}
	}

	@Override
	public ChunkData get( ChunkLocation location ) {
		EnormousChunkMap map = bigData.get( new EnormousChunkLocation( location ) );
		return map == null ? null : map.get( location );
	}
	
	@Override
	public Collection< ChunkLocation > getLocations() {
		return data.keySet();
	}

	@Override
	public boolean contains( ChunkLocation location ) {
		return data.containsKey( location );
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

}
