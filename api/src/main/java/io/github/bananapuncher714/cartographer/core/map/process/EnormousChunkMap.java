package io.github.bananapuncher714.cartographer.core.map.process;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class EnormousChunkMap {
	private static final int CHUNK_WIDTH = 256;

	private EnormousChunkLocation location;
	private ChunkData[] data = new ChunkData[ CHUNK_WIDTH * CHUNK_WIDTH ];
	
	protected EnormousChunkMap( EnormousChunkLocation location ) {
		this.location = new EnormousChunkLocation( location );
	}
	
	public EnormousChunkLocation getLocation() {
		return location;
	}

	public void setLocation( EnormousChunkLocation location ) {
		this.location = location;
	}

	public ChunkData get( ChunkLocation location ) {
		int x = location.getX() & 0xFF;
		int z = location.getZ() & 0xFF;
		
		return data[ x + ( z << 8 ) ];
	}
	
	public void set( ChunkLocation location, ChunkData data ) {
		int x = location.getX() & 0xFF;
		int z = location.getZ() & 0xFF;
		
		this.data[ x + ( z << 8 ) ] = data;
	}
}
