package io.github.bananapuncher714.cartographer.core.map.process;

import java.awt.Color;
import java.io.Serializable;

import org.bukkit.map.MapPalette;

/**
 * Represents a single chunk's worth of data.
 * 
 * Created on 20181128
 * 
 * @author BananaPuncher714
 */
public class ChunkData implements Serializable {
	public static final int CHUNK_WIDTH = 16;
	
	protected final byte[] data;
	
	protected boolean colored = false;
	protected transient byte[] two = new byte[ 64 ];
	protected transient byte[] four = new byte[ 16 ];
	protected transient byte[] eight = new byte[ 4 ];
	protected transient byte mainColor;
	
	public ChunkData( byte[] data ) {
		if ( data.length != CHUNK_WIDTH * CHUNK_WIDTH ) {
			throw new IllegalArgumentException( "Data provided must contain 256 elements!" );
		}
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public byte getDataAt( int x, int z ) {
		return data[ x + z * 16 ];
	}
	
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

	public void refreshColors() {
		mainColor = getBestColor( data );
		for ( int i = 0; i < 8; i++ ) {
	    	for ( int j = 0; j < 8; j++ ) {
	    		two[ i + ( j << 3 ) ] = getBestColor( getSubarray( i << 1, j << 1, 2, 2 ) );
	    	}
	    }
	    for ( int i = 0; i < 4; i++ ) {
	    	for ( int j = 0; j < 4; j++ ) {
	    		four[ i + ( j << 2 ) ] = getBestColor( getSubarray( i << 2, j << 2, 4, 4 ) );
	    	}
	    }
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
