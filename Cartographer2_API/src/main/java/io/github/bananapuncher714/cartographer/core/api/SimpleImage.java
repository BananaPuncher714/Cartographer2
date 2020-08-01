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

/**
 * Load GIFs and images.
 * 
 * @author BananaPuncher714
 */
public class SimpleImage {
	private final long time;
	
	private final BufferedImage[] images;
	private final int[][] data;
	private final int[] delays;
	private final int width;
	private final int height;
	private final int frameCount;
	private final int totalTime;
	
	public SimpleImage( File file ) throws IOException {
		Validate.notNull( file );
		Validate.isTrue( file.exists() );
		Validate.isTrue( file.isFile() );
		
		if ( file.getName().endsWith( ".gif" ) ) {
			FileInputStream stream = new FileInputStream( file );
			GifImage image = GifDecoder.read( new FileInputStream( file ) );
			stream.close();
			
			width = image.getWidth();
			height = image.getHeight();
			
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
				data[ i ] = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( image.getFrame( i ) ) );
		    }
			
			totalTime = sum;
		} else {
			frameCount = 1;
			BufferedImage image = JetpImageUtil.toBufferedImage( ImageIO.read( file ) );
			width = image.getWidth();
			height = image.getHeight();
			images = new BufferedImage[] { image };
			data = new int[][] { JetpImageUtil.getRGBArray( image ) };
			delays = new int[] { 0 };
			totalTime = 0;
		}
		
		time = System.currentTimeMillis();
	}
	
	/**
	 * Construct a SimpleImage with the arguments provided.
	 * 
	 * @param file
	 * Must not be null, exist, and be a file.
	 * @param width
	 * The width that this image should be resized to.
	 * @param height
	 * The height that this image should be resized to.
	 * @param hints
	 * Resizing hints. For example, {@link Image#SCALE_REPLICATE}.
	 * @throws IOException
	 * Look for {@link ImageIO#read( File )}.
	 */
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
	
	/**
	 * Construct a SimpleImage with the arguments provided.
	 * 
	 * @param image
	 * Cannot be null.
	 * @param width
	 * The width that this image should be resized to.
	 * @param height
	 * The height that this image should be resized to.
	 * @param hints
	 * Resizing hints. For example, {@link Image#SCALE_REPLICATE}.
	 */
	public SimpleImage( Image image, int width, int height, int hints ) {
		Validate.notNull( image );
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
	
	/**
	 * Construct a resized SimpleImage with the arguments provided.
	 * 
	 * @param image
	 * Another SimpleImage, cannot be null.
	 * @param width
	 * The new width.
	 * @param height
	 * The new height.
	 * @param hints
	 * Resizing hints. For example, {@link Image#SCALE_REPLICATE}.
	 */
	public SimpleImage( SimpleImage image, int width, int height, int hints ) {
		Validate.notNull( image );
		this.time = image.time;
		this.width = width;
		this.height = height;
		this.frameCount = image.frameCount;
		this.totalTime = image.totalTime;
		this.images = new BufferedImage[ image.images.length ];
		this.delays = new int[ image.delays.length ];
		this.data = new int[ image.data.length ][];
		
		for ( int i = 0; i < image.frameCount; i++ ) {
			BufferedImage rescaledImg = JetpImageUtil.toBufferedImage( image.images[ i ].getScaledInstance( width, height, hints ) );
			images[ i ] = rescaledImg;
			data[ i ] = JetpImageUtil.getRGBArray( rescaledImg );
			delays[ i ] = image.delays[ i ];
		}
	}
	
	/**
	 * Get the current image.
	 * 
	 * @return
	 * If the image is a GIF, then whichever frame should be displayed currently according to the time of creation.
	 */
	public BufferedImage getBufferedImage() {
		return images[ getIndex() ];
	}
	
	/**
	 * Get the image at the given index.
	 * 
	 * @param index
	 * The index of the images.
	 * @return
	 * The image located at the given index.
	 */
	public BufferedImage getBufferedImage( int index ) {
		return images[ index ];
	}
	
	/**
	 * Get the ARGB buffer for the current image.
	 * 
	 * @return
	 * If the image is a GIF, then whichever frame should be displayed currently according to the time of creation.
	 */
	public int[] getImage() {
		return data[ getIndex() ];
	}
	
	/**
	 * Get the ARGB buffer at the given index.
	 * 
	 * @param index
	 * The index of the buffer.
	 * @return
	 * The ARGB buffer located at the given index.
	 */
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
	
	/**
	 * Get the delay at the given index.
	 * 
	 * @param index
	 * The index of the delay.
	 * @return
	 * The delay in milliseconds until the next frame.
	 */
	public int getDelay( int index ) {
		return delays[ index ];
	}
	
	/**
	 * Get the amount of frames.
	 * 
	 * @return
	 * Will return 1 if it is a single image.
	 */
	public int getFrames() {
		return frameCount;
	}
	
	/**
	 * Get the width.
	 * 
	 * @return
	 * The width of all the images.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Get the height.
	 * 
	 * @return
	 * The height of all the images.
	 */
	public int getHeight() {
		return height;
	}
}
