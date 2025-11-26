package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Collection;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public interface ChunkDataStorage {
	void store( ChunkLocation location, ChunkData data );
	void remove( ChunkLocation location );
	ChunkData get( ChunkLocation location );
	Collection< ChunkLocation > getLocations();
	boolean contains( ChunkLocation location );
	byte getColorAt( Location location );
	byte getColorAt( Location location, double scale );
}
