package io.github.bananapuncher714.cartographer.core.file;

import java.io.Serializable;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

/**
 * Contains a square portion of chunks for easier serialization.
 * 
 * @author BananaPuncher714
 */
public class BigChunk implements Serializable {
	/**
	 * Serial id until I can make a better serialization method.
	 */
	private static final long serialVersionUID = 188138088033259619L;

	/**
	 * The width in chunks.
	 */
	public static final int WIDTH = 16;
	
	/**
	 * The BigChunk x coordinate.
	 */
	public final int x;
	
	/**
	 * The BigChunk z coordinate.
	 */
	public final int z;
	private ChunkData[] chunks = new ChunkData[ WIDTH * WIDTH ];
	
	/**
	 * Construct a BigChunk from a {@link ChunkLocation}.
	 * 
	 * @param location
	 * Cannot be null.
	 */
	public BigChunk( ChunkLocation location ) {
		Validate.notNull( location );
		x = location.getX() >> 4;
		z = location.getZ() >> 4;
	}
	
	/**
	 * Construct a BigChunk from BigChunk coordinates.
	 * 
	 * @param x
	 * Should be equivalent to chunk coord / 16.
	 * @param z
	 * Should be equivalent to chunk coord / 16.
	 */
	public BigChunk( int x, int z ) {
		this.x = x;
		this.z = z;
	}
	
	/**
	 * Set the {@link ChunkData} for the {@link ChunkLocation}.
	 * 
	 * @param location
	 * Cannot be null.
	 * @param data
	 * Cannot be null.
	 */
	public void set( ChunkLocation location, ChunkData data ) {
		Validate.notNull( location );
		Validate.notNull( data );
		
		int xOff = location.getX() - ( x << 4 );
		int zOff = location.getZ() - ( z << 4 );
		int index = xOff + zOff * WIDTH;
		Validate.isTrue( chunks.length > index );
		chunks[ index ] = data;
	}
	
	/**
	 * Get the {@link ChunkData}.
	 * 
	 * @return
	 * Mutable array of the {@link ChunkData}.
	 */
	public ChunkData[] getData() {
		return chunks;
	}
}
