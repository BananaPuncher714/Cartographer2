package io.github.bananapuncher714.cartographer.core.file;

import java.io.Serializable;

import org.apache.commons.lang3.Validate;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;

public class BigChunk implements Serializable {
	public final int width = 16;
	public final int x;
	public final int z;
	private ChunkData[] chunks = new ChunkData[ width * width ];
	
	public BigChunk( ChunkLocation location ) {
		x = location.getX() >> 4;
		z = location.getZ() >> 4;
	}
	
	public BigChunkMap getMap() {
		return null;
	}
	
	public void set( ChunkLocation location, ChunkData data ) {
		int xOff = location.getX() - ( x << 4 );
		int zOff = location.getZ() - ( z << 4 );
		int index = xOff + zOff * width;
		Validate.isTrue( chunks.length > index );
		chunks[ index ] = data;
	}
	
	public ChunkData[] getData() {
		return chunks;
	}
}
