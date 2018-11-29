package io.github.bananapuncher714.cartographer.core.renderer;

/**
 * Represents a single chunk's worth of data.
 * 
 * Created on 20181128
 * 
 * @author BananaPuncher714
 */
public class ChunkData {
	public static final int CHUNK_WIDTH = 16;
	
	protected final byte[] data;
	
	public ChunkData( byte[] data ) {
		if ( data.length != CHUNK_WIDTH * CHUNK_WIDTH ) {
			throw new IllegalArgumentException( "Data provided must contain 256 elements!" );
		}
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
}
