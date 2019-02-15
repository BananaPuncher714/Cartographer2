package io.github.bananapuncher714.cartographer.core.test;

import java.util.concurrent.Callable;

public class GenerateTask implements Callable< int[] > {

	@Override
	public int[] call() throws Exception {
		int[] area = new int[ 16 ];
		for ( int i = 0; i < 16; i++ ) {
			int r = ( int ) ( Math.random() * 255 );
			int g = ( int ) ( Math.random() * 255 );
			int b = ( int ) ( Math.random() * 255 );
			area[ i ] = r << 16 | g << 8 | b;
		}
		
		Thread.sleep( 20 );
		
		return area;
	}

}
