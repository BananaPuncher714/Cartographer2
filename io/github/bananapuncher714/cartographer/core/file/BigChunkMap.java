package io.github.bananapuncher714.cartographer.core.file;

public class BigChunkMap {
	long[] data;
	
	public BigChunkMap( Object[] data ) {
		this.data = new long[ ( int ) Math.ceil( data.length / 64.0 ) ];
		
		int index = 0;
		long map = 0;
		int power = 63;
		for ( Object object : data ) {
			if ( object != null ) {
				map |= 1l << power;
			}
			power--;
			if ( power < 0 ) {
				this.data[ index++ ] = map;
				map = 0;
				power = 63;
			}
		}
		if ( index < this.data.length ) {
			this.data[ index ] = map;
		}
	}
	
	public boolean isLoaded( int index ) {
		return ( ( data[ index >> 63 ] >> ( 63 - ( index & 63 ) ) ) & 0b1 ) == 1;
	}
	
	public long[] getData() {
		return data;
	}
}
