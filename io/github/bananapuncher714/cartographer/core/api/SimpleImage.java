package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.util.GifDecoder;
import io.github.bananapuncher714.cartographer.core.util.GifDecoder.GifImage;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class SimpleImage {
	private final long time;
	
	private final BufferedImage[] images;
	private final int[][] data;
	private final int[] delays;
	private final int width;
	private final int height;
	private final int frameCount;
	private final int totalTime;
	
	public SimpleImage( File file, int width, int height, int hints ) throws IOException {
		Validate.notNull( file );
		Validate.isTrue( file.exists() );
		Validate.isTrue( file.isFile() );

		this.width = width;
		this.height = height;
		
		if ( file.getName().endsWith( ".gif" ) ) {
			FileInputStream stream = new FileInputStream( file );
			GifImage image = GifDecoder.read( new FileInputStream( file ) );
			stream.close();
			
			frameCount = image.getFrameCount();
			
			images = new BufferedImage[ frameCount ];
			data = new int[ frameCount ][];
			delays = new int[ frameCount ];
			
			int sum = 0;
			for ( int i = 0; i < frameCount; i++ ) {
				delays[ i ] = image.getDelay( i ) * 10;
				sum += delays[ i ];
				BufferedImage bImage = image.getFrame( i );
				images[ i ] = bImage;
				data[ i ] = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( image.getFrame( i ).getScaledInstance( width, height, hints ) ) );
		    }
			
			totalTime = sum;
		} else {
			frameCount = 1;
			BufferedImage image = JetpImageUtil.toBufferedImage( ImageIO.read( file ).getScaledInstance( width, height, hints ) );
			images = new BufferedImage[] { image };
			data = new int[][] { JetpImageUtil.getRGBArray( image ) };
			delays = new int[] { 0 };
			totalTime = 0;
		}
		
		time = System.currentTimeMillis();
	}
	
	public SimpleImage( Image image, int width, int height, int hints ) {
		BufferedImage bImage = JetpImageUtil.toBufferedImage( image.getScaledInstance( width, height, hints ) );
		images = new BufferedImage[] { bImage };
		data = new int[][] { JetpImageUtil.getRGBArray( bImage ) };
		delays = new int[] { 0 };
		this.width = width;
		this.height = height;
		frameCount = 1;
		
		totalTime = 0;
		time = System.currentTimeMillis();
	}
	
	public BufferedImage getBufferedImage() {
		return images[ getIndex() ];
	}
	
	public BufferedImage getBufferedImage( int index ) {
		return images[ index ];
	}
	
	public int[] getImage() {
		return data[ getIndex() ];
	}
	
	public int[] getImage( int index ) {
		return data[ index ];
	}
	
	protected int getIndex() {
		if ( totalTime == 0 ) {
			return 0;
		}
		
		long left = ( System.currentTimeMillis() - time ) % totalTime;
		int index = -1;
		while ( left >= 0 ) {
			left -= delays[ ++index ];
		}
		return index;
	}
	
	public int getDelay( int index ) {
		return delays[ index ];
	}
	
	public int getFrames() {
		return frameCount;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
