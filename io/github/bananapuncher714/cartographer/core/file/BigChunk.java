package io.github.bananapuncher714.cartographer.core.file;

import java.io.Serializable;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

public class BigChunk implements Serializable {
	public static final int width = 16;
	public final int x;
	public final int z;
	private ChunkData[] chunks = new ChunkData[ width * width ];
	
	public BigChunk( ChunkLocation location ) {
		x = location.getX() >> 4;
		z = location.getZ() >> 4;
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
