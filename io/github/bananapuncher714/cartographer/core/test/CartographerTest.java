package io.github.bananapuncher714.cartographer.core.test;

import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class CartographerTest {
	public static void main( String[] args ) {
		byte[] data = new byte[ 256 ];
		for ( int i = 0; i < 256; i++ ) {
			data[ i ] = ( byte ) i;
		}
		
		byte[] scaled = JetpImageUtil.resize( data, 16, 2, 2 );
		
		for ( int index = 0; index < scaled.length; index++ ) {
			if ( index % 2 == 0 ) {
				System.out.println();
			}
			System.out.print( String.format( "%4d", scaled[ index ] ) );
		}
	}

}
