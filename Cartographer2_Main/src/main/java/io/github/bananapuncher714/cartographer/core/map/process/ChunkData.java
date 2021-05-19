package io.github.bananapuncher714.cartographer.core.map.process;

import java.io.Serializable;

import org.apache.commons.lang.Validate;

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
	public static final int CHUNK_POWER = 4;
	
	protected final byte[] data;
	
	protected boolean colored = false;
	
	/**
	 * Construct a ChunkData with the color data provided.
	 * 
	 * @param data
	 * Cannot be null. Must have length of CHUNK_WIDTH * CHUNK_WIDTH
	 */
	public ChunkData( byte[] data ) {
		Validate.notNull( data );
		Validate.isTrue( data.length == CHUNK_WIDTH * CHUNK_WIDTH, "Data provided must be of " + ( CHUNK_WIDTH * CHUNK_WIDTH ) + " length!" );
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
		return data[ x + ( z << CHUNK_POWER ) ];
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
		return data[ x + ( z << CHUNK_POWER ) ];
	}

	private byte[] getSubarray( int x, int z, int w, int h ) {
		byte[] temp = new byte[ w * h ];
		for ( int i = 0; i < w; i++ ) {
			for ( int j = 0; j < h; j++ ) {
				temp[ i + j * w ] = data[ x + i + ( ( j + z ) << CHUNK_POWER) ];
			}
		}
		return temp;
	}
	
	private byte getBestColor( byte[] items ) {
		int[] array = new int[ 256 ];
		for ( int x = 0; x < items.length; x++ ) {
			array[ ( items[ x ] + 256 ) % 256 ]++;
		}
		
		int max = 0;
		int color = 0;
		for ( int index = 0; index < array.length; index++ ) {
			if ( array[ index ] >= max ) {
				max = array[ index ];
				color = index;
			}
		}
		return ( byte ) color;
	}
}
