package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class SimpleChunkDataStorage implements ChunkDataStorage {
	protected final Map< EnormousChunkLocation, EnormousChunkMap > bigData = null;
	protected final Map< ChunkLocation, ChunkData > data = null;
	
	public SimpleChunkDataStorage() {
	}
	
	@Override
	public void store( ChunkLocation location, ChunkData data ) {
	}

	@Override
	public void remove( ChunkLocation location ) {
	}

	@Override
	public ChunkData get( ChunkLocation location ) {
		return null;
	}
	
	@Override
	public Collection< ChunkLocation > getLocations() {
		return null;
	}

	@Override
	public boolean contains( ChunkLocation location ) {
		return false;
	}

	@Override
	public byte getColorAt( Location location ) {
		return 0;
	}

	@Override
	public byte getColorAt( Location location, double scale ) {
		return 0;
	}

}
