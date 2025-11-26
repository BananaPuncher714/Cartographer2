package io.github.bananapuncher714.cartographer.module.worldviewer.util;

import java.util.Arrays;

/**
 * Big/little endian binary writer
 */
public class BinaryWriter {
	protected byte[] arr;
	protected int pos;
	protected int length = 0;
	
	public BinaryWriter( int startSize ) {
		arr = new byte[ startSize ];
	}
	
	public int seek( int newPos ) {
		int old = pos;
		pos = newPos;
		return old;
	}
	
	public int pos() {
		return pos;
	}
	
	public void write( long val, int length ) {
		for ( int i = length - 1; i >= 0; i-- ) {
			write( ( byte ) ( ( val >> ( i << 3 ) ) & 0xFFL ) );
		}
	}
	
	public void writeLittle( long val, int length ) {
		for ( int i = 0; i < length; i++ ) {
			write( ( byte ) ( ( val >> ( i << 3 ) ) & 0xFFL ) );
		}
	}
	
	public void write( long[] a, int length ) {
		for ( long l : a ) {
			write( l, length );
		}
	}
	
	public void write( byte b ) {
		if ( pos >= arr.length ) {
			byte[] temp = new byte[ arr.length << 1 ];
			for ( int i = 0; i < arr.length; i++ ) {
				temp[ i ] = arr[ i ];
			}
			arr = temp;
		}
		arr[ pos++ ] = b;
		length = Math.max( pos, length );
	}
	
	public void write( String str, int length ) {
		int diff = length - str.length();
		if ( diff < 0 ) {
			str = str.substring( Math.abs( diff ) );
		} else if ( diff > 0 ) {
			char[] empty = new char[ diff ];
			Arrays.fill( empty, ' ' );
			str = new String( empty ) + str;
		}
		
		write( str.getBytes() );
	}
	
	public void write( byte[] a ) {
		for ( byte b : a ) {
			write( b );
		}
	}
	
	public byte[] toByteArray() {
		byte[] fin = new byte[ length ];
		for ( int i = 0; i < length; i++ ) {
			fin[ i ] = arr[ i ];
		}
		return fin;
	}
	
	public void dump() {
		System.out.print( "Array dump of " + length + " with max size " + arr.length );
		for ( int i = 0; i < length; i++ ) {
			if ( i % 8 == 0 ) {
				System.out.println();
			}
			System.out.print( arr[ i ] + "\t" );
		}
		System.out.println();
	}
}
