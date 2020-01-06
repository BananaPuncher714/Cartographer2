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
		if ( !colored ) {
			refreshColors();
		}
		if ( scale == 2 ) {
			return two[ x / 2 + ( z / 2 * 8 ) ];
		} else if ( scale == 4 ) {
			return four[ x / 4 + ( z / 4 * 4 ) ];
		} else if ( scale == 8 ) {
			return eight[ x / 8 + ( z / 8 * 2 ) ];
		} else if ( scale == 16 ) {
			return mainColor;
		} else {
			return data[ x + z * 16 ];
		}
	}

	/**
	 * Refresh the mipmap for this chunk data.
	 */
	public void refreshColors() {
		mainColor = getBestColor( data );
		two = new byte[ 64 ];
		for ( int i = 0; i < 8; i++ ) {
	    	for ( int j = 0; j < 8; j++ ) {
	    		two[ i + ( j << 3 ) ] = getBestColor( getSubarray( i << 1, j << 1, 2, 2 ) );
	    	}
	    }
		four = new byte[ 16 ];
	    for ( int i = 0; i < 4; i++ ) {
	    	for ( int j = 0; j < 4; j++ ) {
	    		four[ i + ( j << 2 ) ] = getBestColor( getSubarray( i << 2, j << 2, 4, 4 ) );
	    	}
	    }
	    eight = new byte[ 4 ];
	    for ( int i = 0; i < 2; i++ ) {
	    	for ( int j = 0; j < 2; j++ ) {
	    		eight[ i + ( j << 1 ) ] = getBestColor( getSubarray( i << 3, j << 3, 8, 8 ) );
	    	}
	    }
	    colored = true;
	}
	
	private byte[] getSubarray( int x, int z, int w, int h ) {
		byte[] temp = new byte[ w * h ];
		for ( int i = 0; i < w; i++ ) {
			for ( int j = 0; j < h; j++ ) {
				temp[ i + j * w ] = data[ x + i + ( ( j + z ) << 4) ];
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
