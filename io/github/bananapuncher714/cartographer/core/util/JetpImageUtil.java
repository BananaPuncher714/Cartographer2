package io.github.bananapuncher714.cartographer.core.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.map.MapPalette;

/**
 * What a piece of optimization;
 * Performs incredibly fast Minecraft color conversion and dithering.
 * 
 * @author jetp250
 */
public final class JetpImageUtil {
	private JetpImageUtil() {
	}
	
	// Test dithering of random colors
	public static void main( String[] args ) {

		int width = 484;
		int[] rgb = new int[ width * 336 ];

		Random random = ThreadLocalRandom.current();

		for ( int i = 0; i < rgb.length; ++i ) {
			rgb[ i ] = random.nextInt() & 0xFFFFFF;
		}

		for ( int i = 0; i < 100; ++i ) {
			for ( int j = 0; j < rgb.length; ++j ) {
				rgb[ j ] = random.nextInt() & 0xFFFFFF;
			}
			long start = System.nanoTime();
			dither( width, rgb );
			long end = System.nanoTime();
			float passed = ( end - start ) / 1000000.0f;
			System.out.printf( "Took %fms%n", passed );
		}
	}

	private static final int[] PALETTE;
	private static final byte[] COLOR_MAP = new byte[ 128 * 128 * 128 ];
	private static final float[] COLOR_MULTIPLIERS = { 0.4375f, 0.1875f, 0.3125f, 0.0625f };

	public final static void init() {
	}

	static {
		List< Integer > colors = new ArrayList< Integer >();

		long start = System.nanoTime();
		for ( int i = 0; i < 256; ++i ) {
			try {
				Color color = MapPalette.getColor( ( byte ) i );
				colors.add( color.getRGB() );
			} catch ( IndexOutOfBoundsException e ) {
				System.out.println( "Captured " + ( i - 1 ) + " colors!" );
				break;
			}
		}
		PALETTE = new int[ colors.size() ];
		int index = 0;
		for ( int color : colors ) {
			PALETTE[ index++ ] = color;
		}
		
		for ( int r = 0; r < 256; r += 2 ) {
			for ( int g = 0; g < 256; g += 2 ) {
				for ( int b = 0; b < 256; b += 2 ) {
					int colorIndex = r >> 1 << 14 | g >> 1 << 7 | b >> 1;
					COLOR_MAP[ colorIndex ] = computeNearest( PALETTE, r, g, b );
				}
			}
		}
		
		long end = System.nanoTime();
		System.out.println( "Initial lookup table initialized in " + ( end - start ) / 1_000_000.0 + " ms" );
	}

	public static byte getBestColor( int rgb ) {
		return COLOR_MAP[ ( rgb >> 16 & 0xFF ) >> 1 << 14 | ( rgb >> 8 & 0xFF ) >> 1 << 7 | ( rgb & 0xFF ) >> 1 ];
	}

	public static byte getBestColor( int red, int green, int blue ) {
		return COLOR_MAP[ red >> 1 << 14 | green >> 1 << 7 | blue >> 1 ];
	}
	
	private static byte computeNearest( int[] palette, int red, int green, int blue ) {
		int val = 0;
		float best_distance = Float.MAX_VALUE;
		for (int i = 4; i < palette.length; ++i) {
			int col = palette[i];
			float distance = getDistance(red, green, blue, col >> 16 & 0xFF, col >> 8 & 0xFF, col & 0xFF);
			if (distance < best_distance) {
				best_distance = distance;
				val = i;
			}
		}
		return (byte) val;
	}
	
	private static float getDistance( int red, int green, int blue, int red2, int green2, int blue2 ) {
		float red_avg = ( red + red2 ) * .5f;
		int r = red - red2;
		int g = green - green2;
		int b = blue - blue2;
		float weight_red = 2.0f + red_avg * ( 1f / 256f );
		float weight_green = 4.0f;
		float weight_blue = 2.0f + ( 255.0f - red_avg ) * ( 1f / 256f );
		return weight_red * r * r + weight_green * g * g + weight_blue * b * b;
	}

	public static byte[] simplify( int[] buffer ) {
		byte[] map = new byte[ buffer.length ];
		for (int index = 0; index < buffer.length; index++) {
			int rgb = buffer[ index ];
			int red = rgb >> 16 & 0xFF;
			int green = rgb >> 8 & 0xFF;
			int blue = rgb & 0xFF;
			byte ptr = getBestColor( red, green, blue );
			map[ index ] = ptr;
		}
		return map;
	}

	public static byte[] dither( Image image ) {
		BufferedImage bImage = toBufferedImage( image );
		return dither( bImage.getWidth(), bImage.getRGB( 0, 0, bImage.getWidth(), bImage.getHeight(), null, 0, bImage.getWidth() ) );
	}
	
	/**
	 * Dither an rgb buffer
	 * 
	 * @param width
	 * The width of the image
	 * @param buffer
	 * RGB buffer
	 * @return
	 * Dithered image in minecraft colors
	 */
	public static byte[] dither( int width, int[] buffer ) {
		int height = buffer.length / width;

		float[] mult = COLOR_MULTIPLIERS;

		int[][] dither_buffer = new int[ 2 ][ Math.max( width, height ) * 3 ];

		byte[] map = new byte[ buffer.length ];
		int[] y_temps = { 0, 1, 1, 1 };
		int[] x_temps = { 1, -1, 0, 1 };
		for (int x = 0; x < width; ++x) {
			dither_buffer[ 0 ] = dither_buffer[ 1 ];
			dither_buffer[ 1 ] = new int[ Math.max( width, height ) * 3 ];
			int[] buffer2 = dither_buffer[ 0 ];
			for ( int y = 0; y < height; ++y ) {
				int rgb = buffer[ y * width + x ];

				int red   = rgb >> 16 & 0xFF;
				int green = rgb >> 8  & 0xFF;
				int blue  = rgb       & 0xFF;
				
				int index = y + ( y << 1 );

				red   = ( red   += buffer2[ index++ ] ) > 255 ? 255 : red   < 0 ? 0 : red;
				green = ( green += buffer2[ index++ ] ) > 255 ? 255 : green < 0 ? 0 : green;
				blue  = ( blue  += buffer2[ index   ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
				int matched_color = PALETTE[ Byte.toUnsignedInt( getBestColor( red, green, blue ) ) ];
				int delta_r = red   - ( matched_color >> 16 & 0xFF );
				int delta_g = green - ( matched_color >> 8  & 0xFF );
				int delta_b = blue  - ( matched_color       & 0xFF );
				for ( int i = 0; i < x_temps.length; i++ ) {
					int temp_y = y_temps[ i ];
					int temp_x;
					if ( temp_y < height && ( temp_x = y + x_temps[i] ) < width && temp_x > 0 ) {
						int[] buffer3 = dither_buffer[ temp_y ];
						float scalar = mult[ i ];
						index = temp_x + ( temp_x << 1 );
						buffer3[ index ] = ( int ) ( buffer3[index++] + scalar * delta_r );
						buffer3[ index ] = ( int ) ( buffer3[index++] + scalar * delta_g );
						buffer3[ index ] = ( int ) ( buffer3[index  ] + scalar * delta_b );
					}
				}
				if ( ( rgb >> 24 & 0xFF ) < 0x80 ) {
					map[ y * width + x ] = 0;
				} else {
					map[ y * width + x ] = COLOR_MAP[ red >> 1 << 14 | green >> 1 << 7 | blue >> 1 ];
				}
			}
		}
		return map;
	}
	
	public static int[] getSubImage( int topCornerX, int topCornerY, int width, int height, int[] image, int imageWidth ) {
		int[] subimage = new int[ width * height ];
		
		int imageHeight = image.length / imageWidth;
		
		int topX = Math.max( 0, topCornerX );
		int topY = Math.max( 0, topCornerY );
		
		int imgWidth = Math.min( imageWidth - topCornerX, width );
		int imgHeight = Math.min( imageHeight - topCornerY, height );
		
		for ( int x = 0; x < imgWidth; x++ ) {
			for ( int y = 0; y < imgHeight; y++ ) {
				subimage[ x + y * width ] = image[ x + topX + ( y + topY ) * imageWidth ];
			}
		}
		return subimage;
	}

	public static void overlay( int x, int y, int[] image, int imageWidth, int[] canvas, int canvasWidth ) {
		int width = canvasWidth;
		int height = canvas.length / canvasWidth;
		int imageHeight = image.length / imageWidth;
		
		int[] widthData = JetpImageUtil.getSubsegment( 0, width, x, imageWidth );
		int[] heightData = JetpImageUtil.getSubsegment( 0, height, y, imageHeight );
		
		int widthStart = widthData[ 0 ];
		int widthEnd = widthData[ 1 ];
		int widthLength = widthEnd - widthStart;
		
		int heightStart = heightData[ 0 ];
		int heightEnd = heightData[ 1 ];
		int heightLength = heightEnd - heightStart;
		
		for ( int offY = 0; offY < heightLength; offY++ ) {
			int canvasIndexY = ( heightStart + offY ) * width + widthStart;
			int imageIndexY = ( heightStart - y + offY ) * imageWidth;
			for ( int offX = 0; offX < widthLength; offX++ ) {
				canvas[ offX + canvasIndexY ] = image[ widthStart - x + offX + imageIndexY ];
			}
		}
	}
	
	public static BufferedImage toBufferedImage( Image img ) {
		if ( img instanceof BufferedImage ) {
			return ( BufferedImage ) img;
		}

		BufferedImage bimage = new BufferedImage( img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB );

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		return bimage;
	}
	
	public static int[] getRGBArray( BufferedImage image ) {
		return image.getRGB( 0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth() );
	}
	
	public static int[] getSubsegment( int start, int length, int substart, int sublength ) {
		int relativeStart = Math.min( start + length, Math.max( start, substart ) );
		int relativeEnd = Math.max( start, Math.min( start + length, substart + sublength ) );
		return new int[] { relativeStart, relativeEnd };
	}
	
	public static byte[] resize( byte[] data, int originalWidth, int width, int height ) {
		int size = width * height;
		byte[] scaled = new byte[ size ];
		
		int originalHeight = data.length / originalWidth;
		
		double widthRatio = originalWidth / ( double ) width;
		double heightRatio = originalHeight / ( double ) height;
		
		for ( int y = 0; y < height; y++ ) {
			int scaledY = ( int ) ( y * heightRatio ) * originalWidth;
			int yHeight = y * width;
			for ( int x = 0; x < width; x++ ) {
				int scaledX = ( int ) ( x * widthRatio );
			
				scaled[ x + yHeight ] = data[ scaledX + scaledY ];
			}
		}
		
		return scaled;
	}
	
	public static int overwriteColor( int baseColor, int overlay ) {
		int a2 = overlay >> 24 & 0xFF;
		if ( a2 == 255 ) {
			return overlay;
		} else if ( a2 == 0 ) {
			return baseColor;
		}
		int r2 = overlay >> 16 & 0xFF;
	    int g2 = overlay >> 8  & 0xFF;
	    int b2 = overlay       & 0xFF;
		
	    int a1 = Math.max( baseColor >> 24 & 0xFF, a2 );
		int r1 = baseColor >> 16 & 0xFF;
	    int g1 = baseColor >> 8  & 0xFF;
	    int b1 = baseColor       & 0xFF;
	    
	    double percent = a2 / 255.0;
	    double unPercent = 1 - percent;
	    
	    int r = ( int ) ( r1 * unPercent + r2 * percent );
	    int g = ( int ) ( g1 * unPercent + g2 * percent );
	    int b = ( int ) ( b1 * unPercent + b2 * percent );
	    
	    return a1 << 24 | r << 16 | g << 8 | b;
	}
	
	public static int mixColors( int color1, int color2 ) {
		int a2 = color2 >> 24 & 0xFF;
		int r2 = color2 >> 16 & 0xFF;
	    int g2 = color2 >> 8  & 0xFF;
	    int b2 = color2       & 0xFF;
		
		int r1 = color1 >> 16 & 0xFF;
	    int g1 = color1 >> 8  & 0xFF;
	    int b1 = color1       & 0xFF;

	    double percent = a2 / 255.0;
	    
	    int r = ( int ) ( ( r1 + ( r2 * percent ) ) / 2 );
	    int g = ( int ) ( ( g1 + ( g2 * percent ) ) / 2 );
	    int b = ( int ) ( ( b1 + ( b2 * percent ) ) / 2 );
	    
	    return r << 16 | g << 8 | b;
	}
}