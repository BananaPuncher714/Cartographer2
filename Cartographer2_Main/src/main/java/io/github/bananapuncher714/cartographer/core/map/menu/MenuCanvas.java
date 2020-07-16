package io.github.bananapuncher714.cartographer.core.map.menu;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MenuCanvas {
	protected int height;
	protected int width;
	protected int[] data;
	protected byte[] displayData;
	protected boolean dither = true;
	protected boolean dirty = false;
	protected Set< MapCursor > cursors = new HashSet< MapCursor >();
	
	public MenuCanvas( int width, int height ) {
		this.width = width;
		this.height = height;
		
		data = new int[ width * height ];
		displayData = new byte[ width * height ];
	}
	
	public void setDither( boolean dither ) {
		if ( this.dither != dither ) {
			this.dither = dither;
			
			if ( dither ) {
				displayData = JetpImageUtil.dither2Minecraft( data, width ).array();
			} else {
				displayData = JetpImageUtil.simplify( data );
			}
		}
	}
	
	public void setPixel( int x, int y, Color color ) {
		data[ x + y * width ] = color.getRGB();
		markDirty();
	}

	public void drawPixel( int x, int y, Color color ) {
		data[ x + y * width ] = JetpImageUtil.overwriteColor( data[ x + y * width ], color.getRGB() );
		markDirty();
	}
	
	public void drawRectangle( int x, int y, int width, int height, Color color ) {
		x = Math.max( 0, x );
		y = Math.max( 0, y );
		width = Math.min( width, this.width - x );
		height = Math.min( height, this.height - y );
		int rgb = color.getRGB();
		
		for ( int py = 0; py < height; py++ ) {
			int yVal = ( py + y ) * this.width;
			for ( int px = 0; px < width; px++ ) {
				int index = yVal + px + x;
				data[ index ] = JetpImageUtil.overwriteColor( data[ index ], rgb );
			}
		}
		markDirty();
	}
	
	public void drawCanvas( MenuCanvas canvas, int x, int y ) {
		drawImage( canvas.data, canvas.width, x, y );
	}
	
	public void drawImage( SimpleImage image, int x, int y ) {
		drawImage( image.getBufferedImage(), x, y );
	}
	
	public void drawImage( Image image, int x, int y ) {
		BufferedImage bImage = JetpImageUtil.toBufferedImage( image );
		drawImage( JetpImageUtil.getRGBArray( bImage ), bImage.getWidth(), x, y );
	}

	public void drawImage( int[] image, int imageWidth, int x, int y ) {
		int imageHeight = image.length / imageWidth;
		
		int[] globalX = JetpImageUtil.getSubsegment( 0, width, x, imageWidth );
		int gxStart = globalX[ 0 ];
		int gxEnd = globalX[ 1 ];
		
		int[] globalY = JetpImageUtil.getSubsegment( 0, height, y, imageHeight );
		int gyStart = globalY[ 0 ];
		int gyEnd = globalY[ 1 ];
		
		for ( int py = gyStart; py < gyEnd; py++ ) {
			int cy = py * width;
			int iy = ( py - y ) * imageWidth;
			for ( int px = gxStart; px < gxEnd; px++ ) {
				int cx = px;
				int ix = px - x;
				
				int prevColor = data[ cx + cy ];
				data[ cx + cy ] = JetpImageUtil.overwriteColor( prevColor, image[ ix + iy ] );
			}
		}
		markDirty();
	}
	
	public void drawLine( int x1, int y1, int x2, int y2, Color color ) {
		int s = Math.max( Math.abs( x1 - x2 ), Math.abs( y1 - y2 ) );
		for ( double i = 0; i <= s; i++ ) {
			int xPoint = ( int ) ( x2 + ( x1 - x2 ) * ( i / s ) );
			int yPoint = ( int ) ( y2 + ( y1 - y2 ) * ( i / s ) );
			if ( xPoint >= 0 && yPoint >= 0 && xPoint < width && yPoint < height) {
				drawPixel( xPoint, yPoint, color );
			}
		}
	}
	
	public void fill( Color color ) {
		Arrays.fill( data, color.getRGB() );
		markDirty();
	}
	
	public void clear() {
		data = new int[ width * height ];
		markDirty();
	}
	
	public Color getPixel( int x, int y ) {
		return new Color( data[ x + y * width ], true );
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int[] getData() {
		return data;
	}

	protected void markDirty() {
		dirty = true;
	}
	
	protected void update() {
		if ( dither ) {
			displayData = JetpImageUtil.dither2Minecraft( data, width ).array();
		} else {
			displayData = JetpImageUtil.simplify( data );
		}
		dirty = false;
	}
	
	public boolean isDither() {
		return dither;
	}
	
	public byte[] getDisplay() {
		if ( dirty ) {
			update();
		}
		return displayData;
	}
	
	public Collection< MapCursor > getCursors() {
		return cursors;
	}
	
	public void apply( Frame frame ) {
		int[] frameDisplay = frame.getDisplay();
		int frameHeight = frameDisplay.length / frame.width;
		
		int topX = Math.max( 0, frame.x );
		int topY = Math.max( 0, frame.y );
		
		int width = Math.min( this.width - frame.x, frame.width );
		int height = Math.min( this.height - frame.y, frameHeight );
		
		for ( int y = 0; y < height; y++ ) {
			int ly = ( y + topY ) * width;
			int fy = y * frame.width;
			for ( int x = 0; x < width; x++ ) {
				int lx = x + topX;
				int prev = data[ lx + ly ];
				data[ lx + ly ] = JetpImageUtil.overwriteColor( prev, frameDisplay[ x + fy ] );
			}
		}
		markDirty();
	}
}
