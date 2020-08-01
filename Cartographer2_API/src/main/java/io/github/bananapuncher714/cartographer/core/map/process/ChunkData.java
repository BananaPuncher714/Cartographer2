package io.github.bananapuncher714.cartographer.core.map.process;

import java.io.Serializable;

/**
 * Represents a single chunk's worth of data.
 * 
 * @author BananaPuncher714
 */
public final class ChunkData implements Serializable {
	/**
	 * Serial id until I can make a better serialization method.
	 */
	private static final long serialVersionUID = -3618660491621607445L;

	/**
	 * Width of the chunk, not very relevant currently.
	 */
	public static final int CHUNK_WIDTH = 16;
	
	protected final byte[] data;
	
	protected boolean colored = false;
	protected transient byte[] two;
	protected transient byte[] four;
	protected transient byte[] eight;
	protected transient byte mainColor;
	
	/**
	 * Construct a ChunkData with the color data provided.
	 * 
	 * @param data
	 * Cannot be null. Must have length of CHUNK_WIDTH * CHUNK_WIDTH
	 */
	public ChunkData( byte[] data ) {
		this.data = data;
	}
	
	/**
	 * Get the data for this chunk.
	 * 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Get the data at the coordinates specified.
	 * 
	 * @param x
	 * X coordinate from 0 to 15.
	 * @param z
	 * Z coordinate from 0 to 15.
	 * @return
	 * A byte representing the color.
	 */
	public byte getDataAt( int x, int z ) {
		return data[ x + z * 16 ];
	}
	
	/**
	 * Get the data at the coordinates specified using the mipmaps.
	 * 
	 * @param x
	 * X coordinate from 0 to 15.
	 * @param z
	 * Z coordinate from 0 to 15.
	 * @param scale
	 * Scale in blocks per pixel
	 * @return
	 * A byte representing the color.
	 */
	public byte getDataAt( int x, int z, double scale ) {
		return 0;
	}

	/**
	 * Refresh the mipmap for this chunk data.
	 */
	public void refreshColors() {
	}
}
