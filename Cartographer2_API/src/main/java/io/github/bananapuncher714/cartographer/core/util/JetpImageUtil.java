package io.github.bananapuncher714.cartographer.core.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.map.MapPalette;

/**
 * What a piece of optimization;
 * Performs incredibly fast Minecraft color conversion and dithering.
 * 
 * @author jetp250
 */
public final class JetpImageUtil {
	private static int largest = 0;
	
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
			dither( rgb, width );
			long end = System.nanoTime();
			float passed = ( end - start ) / 1000000.0f;
			System.out.printf( "Took %fms%n", passed );
		}
	}

	private static final int[] PALETTE;
	private static final byte[] COLOR_MAP = new byte[ 128 * 128 * 128 ];
	private static final int[] FULL_COLOR_MAP = new int[ 128 * 128 * 128 ];
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
				largest = i - 1;
				break;
			}
		}
		PALETTE = new int[ colors.size() ];
		int index = 0;
		for ( int color : colors ) {
			PALETTE[ index++ ] = color;
		}
		PALETTE[ 0 ] = 0;
		
		// ForkJoinPool'd the loading of colors
		List< LoadRed > tasks = new ArrayList< LoadRed >( 128 );
		for ( int r = 0; r < 256; r += 2 ) {
			LoadRed red = new LoadRed( PALETTE, r );
			
			tasks.add( red );
			
			red.fork();
		}
		
		for ( int i = 0; i < 128; i++ ) {
			byte[] sub = tasks.get( i ).join();
			int ci = i << 14;
			for ( int si = 0; si < 16384; si++ ) {
				COLOR_MAP[ ci + si ] = sub[ si ];
				FULL_COLOR_MAP[ ci + si ] = PALETTE[ Byte.toUnsignedInt( sub[ si ] ) ];
			}
		}
		
		// Original method
//		for ( int r = 0; r < 256; r += 2 ) {
//			for ( int g = 0; g < 256; g += 2 ) {
//				for ( int b = 0; b < 256; b += 2 ) {
//					int colorIndex = r >> 1 << 14 | g >> 1 << 7 | b >> 1;
//
//					int val = 0;
//					float best_distance = Float.MAX_VALUE;
//					float distance = 0;
//					int col = 0;
//					for (int i = 4; i < PALETTE.length; ++i) {
//						col = PALETTE[i];
//						int r2 = col >> 16 & 0xFF;
//						int g2 = col >> 8 & 0xFF;
//						int b2 = col & 0xFF;
//				
//						float red_avg = ( r + r2 ) * .5f;
//						int redVal = r - r2;
//						int greenVal = g - g2;
//						int blueVal = b - b2;
//						float weight_red = 2.0f + red_avg * ( 1f / 256f );
//						float weight_green = 4.0f;
//						float weight_blue = 2.0f + ( 255.0f - red_avg ) * ( 1f / 256f );
//						distance = weight_red * redVal * redVal + weight_green * greenVal * greenVal + weight_blue * blueVal * blueVal;
//				
//						if (distance < best_distance) {
//							best_distance = distance;
//							val = i;
//						}
//					}
//					COLOR_MAP[ colorIndex ] = ( byte ) val;
//				}
//			}
//		}

		long end = System.nanoTime();
		System.out.println( "Initial lookup table initialized in " + ( end - start ) / 1_000_000.0 + " ms" );
	}

	public static int getLargestColorVal() {
		return largest;
	}
	
	public static int getColorFromMinecraftPalette( byte val ) {
		return PALETTE[ ( val + 256 ) % 256 ];
	}

	public static byte getBestColorIncludingTransparent( int rgb ) {
		return ( rgb >>> 24 & 0xFF ) == 0 ? 0 : getBestColor( rgb );
	}
	
	public static byte getBestColor( int rgb ) {
		return COLOR_MAP[ ( rgb >> 16 & 0xFF ) >> 1 << 14 | ( rgb >> 8 & 0xFF ) >> 1 << 7 | ( rgb & 0xFF ) >> 1 ];
	}

	public static byte getBestColor( int red, int green, int blue ) {
		return COLOR_MAP[ red >> 1 << 14 | green >> 1 << 7 | blue >> 1 ];
	}
	
	public static int getBestFullColor( int red, int green, int blue ) {
		return FULL_COLOR_MAP[ red >> 1 << 14 | green >> 1 << 7 | blue >> 1 ];
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

	public static BufferedImage ditherImage( Image image ) {
		BufferedImage bImage = toBufferedImage( image );
		byte[] dithered = dither( image );
		int[] argb = new int[ dithered.length ];
		for ( int i = 0; i < dithered.length; i++ ) {
			argb[ i ] = getColorFromMinecraftPalette( dithered[ i ] );
		}
		BufferedImage newImage = new BufferedImage( bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_ARGB );
		newImage.setRGB( 0, 0, bImage.getWidth(), bImage.getHeight(), argb, 0, bImage.getWidth() );
		return newImage;
	}
	
	public static byte[] dither( Image image ) {
		BufferedImage bImage = toBufferedImage( image );
		return dither2Minecraft( bImage.getRGB( 0, 0, bImage.getWidth(), bImage.getHeight(), null, 0, bImage.getWidth() ), bImage.getWidth() ).array();
	}
	
	/**
	 * Floyd-steinberg dithering with serpentine scanning
	 */
	public static void dither( int[] buffer, int width ) {
		int height = buffer.length / width;

		int widthMinus = width - 1;
		int heightMinus = height - 1;
		
		int[][] dither_buffer = new int[ 2 ][ width + width << 1 ];
		
		for ( int y = 0; y < height; y++ ) {
			boolean hasNextY = y < heightMinus;
			
			int yIndex = y * width;
			if ( y % 2 == 0 ) {
				// Go left to right
				int bufferIndex = 0;
				int[] buf1 = dither_buffer[ 0 ];
				int[] buf2 = dither_buffer[ 1 ];
				for ( int x = 0; x < width; x++ ) {
					boolean hasPrevX = x > 0;
					boolean hasNextX = x < widthMinus;
					
					int index = yIndex + x;
					int rgb = buffer[ index ];
					
					int red   = rgb >> 16 & 0xFF;
					int green = rgb >> 8  & 0xFF;
					int blue  = rgb       & 0xFF;
					
					// Get the previous error and add
					red   = ( red   += buf1[ bufferIndex++ ] ) > 255 ? 255 : red   < 0 ? 0 : red;
					green = ( green += buf1[ bufferIndex++ ] ) > 255 ? 255 : green < 0 ? 0 : green;
					blue  = ( blue  += buf1[ bufferIndex++ ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
					
					// Get the closest color to the modified pixel
					int closest = getBestFullColor( red, green, blue );
					
					// Find the error
					int delta_r = red   - ( closest >> 16 & 0xFF );
					int delta_g = green - ( closest >> 8  & 0xFF );
					int delta_b = blue  - ( closest       & 0xFF );
					
					// Add to the next pixel
					if ( hasNextX ) {
						buf1[ bufferIndex     ] = ( int ) ( 0.4375 * delta_r );
						buf1[ bufferIndex + 1 ] = ( int ) ( 0.4375 * delta_g );
						buf1[ bufferIndex + 2 ] = ( int ) ( 0.4375 * delta_b );
					}
					
					if ( hasNextY ) {
						if ( hasPrevX ) {
							buf2[ bufferIndex - 6 ] = ( int ) ( 0.1875 * delta_r );
							buf2[ bufferIndex - 5 ] = ( int ) ( 0.1875 * delta_g );
							buf2[ bufferIndex - 4 ] = ( int ) ( 0.1875 * delta_b );
						}
						
						buf2[ bufferIndex - 3 ] = ( int ) ( 0.3125 * delta_r );
						buf2[ bufferIndex - 2 ] = ( int ) ( 0.3125 * delta_g );
						buf2[ bufferIndex - 1 ] = ( int ) ( 0.3125 * delta_b );
						
						if ( hasNextX ) {
							buf2[ bufferIndex     ] = ( int ) ( 0.0625 * delta_r );
							buf2[ bufferIndex + 1 ] = ( int ) ( 0.0625 * delta_g );
							buf2[ bufferIndex + 2 ] = ( int ) ( 0.0625 * delta_b );
						}
					}
					
					buffer[ index ] = closest;
				}
			} else {
				// Go right to left
				int bufferIndex = width + ( width << 1 ) - 1;
				int[] buf1 = dither_buffer[ 1 ];
				int[] buf2 = dither_buffer[ 0 ];
				for ( int x = width - 1; x >= 0; x-- ) {
					boolean hasPrevX = x < widthMinus;
					boolean hasNextX = x > 0;
					
					int index = yIndex + x;
					int rgb = buffer[ index ];
					
					int red   = rgb >> 16 & 0xFF;
					int green = rgb >> 8  & 0xFF;
					int blue  = rgb       & 0xFF;
					
					// Get the previous error and add
					blue  = ( blue  += buf1[ bufferIndex-- ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
					green = ( green += buf1[ bufferIndex-- ] ) > 255 ? 255 : green < 0 ? 0 : green;
					red   = ( red   += buf1[ bufferIndex-- ] ) > 255 ? 255 : red   < 0 ? 0 : red;
					
					// Get the closest color to the modified pixel
					int closest = getBestFullColor( red, green, blue );
					
					// Find the error
					int delta_r = red   - ( closest >> 16 & 0xFF );
					int delta_g = green - ( closest >> 8  & 0xFF );
					int delta_b = blue  - ( closest       & 0xFF );
					
					// Add to the next pixel
					if ( hasNextX ) {
						buf1[ bufferIndex     ] = ( int ) ( 0.4375 * delta_b );
						buf1[ bufferIndex - 1 ] = ( int ) ( 0.4375 * delta_g );
						buf1[ bufferIndex - 2 ] = ( int ) ( 0.4375 * delta_r );
					}
					
					if ( hasNextY ) {
						if ( hasPrevX ) {
							buf2[ bufferIndex + 6 ] = ( int ) ( 0.1875 * delta_b );
							buf2[ bufferIndex + 5 ] = ( int ) ( 0.1875 * delta_g );
							buf2[ bufferIndex + 4 ] = ( int ) ( 0.1875 * delta_r );
						}
						
						buf2[ bufferIndex + 3 ] = ( int ) ( 0.3125 * delta_b );
						buf2[ bufferIndex + 2 ] = ( int ) ( 0.3125 * delta_g );
						buf2[ bufferIndex + 1 ] = ( int ) ( 0.3125 * delta_r );
						
						if ( hasNextX ) {
							buf2[ bufferIndex     ] = ( int ) ( 0.0625 * delta_b );
							buf2[ bufferIndex - 1 ] = ( int ) ( 0.0625 * delta_g );
							buf2[ bufferIndex - 2 ] = ( int ) ( 0.0625 * delta_r );
						}
					}
					
					buffer[ index ] = closest;
				}
			}
		}
	}
	
	public static ByteBuffer dither2Minecraft( int[] buffer, int width ) {
		int height = buffer.length / width;

		int widthMinus = width - 1;
		int heightMinus = height - 1;
		
		int[][] dither_buffer = new int[ 2 ][ width + width << 1 ];
		
		ByteBuffer data = ByteBuffer.allocate( buffer.length );
		for ( int y = 0; y < height; y++ ) {
			boolean hasNextY = y < heightMinus;
			
			int yIndex = y * width;
			if ( y % 2 == 0 ) {
				// Go left to right
				int bufferIndex = 0;
				int[] buf1 = dither_buffer[ 0 ];
				int[] buf2 = dither_buffer[ 1 ];
				for ( int x = 0; x < width; x++ ) {
					boolean hasPrevX = x > 0;
					boolean hasNextX = x < widthMinus;
					
					int index = yIndex + x;
					int rgb = buffer[ index ];
					
					int red   = rgb >> 16 & 0xFF;
					int green = rgb >> 8  & 0xFF;
					int blue  = rgb       & 0xFF;
					
					// Get the previous error and add
					red   = ( red   += buf1[ bufferIndex++ ] ) > 255 ? 255 : red   < 0 ? 0 : red;
					green = ( green += buf1[ bufferIndex++ ] ) > 255 ? 255 : green < 0 ? 0 : green;
					blue  = ( blue  += buf1[ bufferIndex++ ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
					
					// Get the closest color to the modified pixel
					int closest = getBestFullColor( red, green, blue );
					
					// Find the error
					int delta_r = red   - ( closest >> 16 & 0xFF );
					int delta_g = green - ( closest >> 8  & 0xFF );
					int delta_b = blue  - ( closest       & 0xFF );
					
					// Add to the next pixel
					if ( hasNextX ) {
						buf1[ bufferIndex     ] = ( int ) ( 0.4375 * delta_r );
						buf1[ bufferIndex + 1 ] = ( int ) ( 0.4375 * delta_g );
						buf1[ bufferIndex + 2 ] = ( int ) ( 0.4375 * delta_b );
					}
					
					if ( hasNextY ) {
						if ( hasPrevX ) {
							buf2[ bufferIndex - 6 ] = ( int ) ( 0.1875 * delta_r );
							buf2[ bufferIndex - 5 ] = ( int ) ( 0.1875 * delta_g );
							buf2[ bufferIndex - 4 ] = ( int ) ( 0.1875 * delta_b );
						}
						
						buf2[ bufferIndex - 3 ] = ( int ) ( 0.3125 * delta_r );
						buf2[ bufferIndex - 2 ] = ( int ) ( 0.3125 * delta_g );
						buf2[ bufferIndex - 1 ] = ( int ) ( 0.3125 * delta_b );
						
						if ( hasNextX ) {
							buf2[ bufferIndex     ] = ( int ) ( 0.0625 * delta_r );
							buf2[ bufferIndex + 1 ] = ( int ) ( 0.0625 * delta_g );
							buf2[ bufferIndex + 2 ] = ( int ) ( 0.0625 * delta_b );
						}
					}
					
					data.put( index, getBestColor( closest ) );
				}
			} else {
				// Go right to left
				int bufferIndex = width + ( width << 1 ) - 1;
				int[] buf1 = dither_buffer[ 1 ];
				int[] buf2 = dither_buffer[ 0 ];
				for ( int x = width - 1; x >= 0; x-- ) {
					boolean hasPrevX = x < widthMinus;
					boolean hasNextX = x > 0;
					
					int index = yIndex + x;
					int rgb = buffer[ index ];
					
					int red   = rgb >> 16 & 0xFF;
					int green = rgb >> 8  & 0xFF;
					int blue  = rgb       & 0xFF;
					
					// Get the previous error and add
					blue  = ( blue  += buf1[ bufferIndex-- ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
					green = ( green += buf1[ bufferIndex-- ] ) > 255 ? 255 : green < 0 ? 0 : green;
					red   = ( red   += buf1[ bufferIndex-- ] ) > 255 ? 255 : red   < 0 ? 0 : red;
					
					// Get the closest color to the modified pixel
					int closest = getBestFullColor( red, green, blue );
					
					// Find the error
					int delta_r = red   - ( closest >> 16 & 0xFF );
					int delta_g = green - ( closest >> 8  & 0xFF );
					int delta_b = blue  - ( closest       & 0xFF );
					
					// Add to the next pixel
					if ( hasNextX ) {
						buf1[ bufferIndex     ] = ( int ) ( 0.4375 * delta_b );
						buf1[ bufferIndex - 1 ] = ( int ) ( 0.4375 * delta_g );
						buf1[ bufferIndex - 2 ] = ( int ) ( 0.4375 * delta_r );
					}
					
					if ( hasNextY ) {
						if ( hasPrevX ) {
							buf2[ bufferIndex + 6 ] = ( int ) ( 0.1875 * delta_b );
							buf2[ bufferIndex + 5 ] = ( int ) ( 0.1875 * delta_g );
							buf2[ bufferIndex + 4 ] = ( int ) ( 0.1875 * delta_r );
						}
						
						buf2[ bufferIndex + 3 ] = ( int ) ( 0.3125 * delta_b );
						buf2[ bufferIndex + 2 ] = ( int ) ( 0.3125 * delta_g );
						buf2[ bufferIndex + 1 ] = ( int ) ( 0.3125 * delta_r );
						
						if ( hasNextX ) {
							buf2[ bufferIndex     ] = ( int ) ( 0.0625 * delta_b );
							buf2[ bufferIndex - 1 ] = ( int ) ( 0.0625 * delta_g );
							buf2[ bufferIndex - 2 ] = ( int ) ( 0.0625 * delta_r );
						}
					}
					
					data.put( index, getBestColor( closest ) );
				}
			}
		}
		return data;
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
//	public static byte[] dither( int width, int[] buffer ) {
//		int height = buffer.length / width;
//
//		float[] mult = COLOR_MULTIPLIERS;
//
//		int[][] dither_buffer = new int[ 2 ][ Math.max( width, height ) * 3 ];
//
//		byte[] map = new byte[ buffer.length ];
//		int[] y_temps = { 0, 1, 1, 1 };
//		int[] x_temps = { 1, -1, 0, 1 };
//		for (int x = 0; x < width; ++x) {
//			dither_buffer[ 0 ] = dither_buffer[ 1 ];
//			dither_buffer[ 1 ] = new int[ Math.max( width, height ) * 3 ];
//			int[] buffer2 = dither_buffer[ 0 ];
//			for ( int y = 0; y < height; ++y ) {
//				int rgb = buffer[ y * width + x ];
//
//				int red   = rgb >> 16 & 0xFF;
//				int green = rgb >> 8  & 0xFF;
//				int blue  = rgb       & 0xFF;
//				
//				int index = y + ( y << 1 );
//
//				red   = ( red   += buffer2[ index++ ] ) > 255 ? 255 : red   < 0 ? 0 : red;
//				green = ( green += buffer2[ index++ ] ) > 255 ? 255 : green < 0 ? 0 : green;
//				blue  = ( blue  += buffer2[ index   ] ) > 255 ? 255 : blue  < 0 ? 0 : blue;
//				int matched_color = PALETTE[ Byte.toUnsignedInt( getBestColor( red, green, blue ) ) ];
//				int delta_r = red   - ( matched_color >> 16 & 0xFF );
//				int delta_g = green - ( matched_color >> 8  & 0xFF );
//				int delta_b = blue  - ( matched_color       & 0xFF );
//				for ( int i = 0; i < x_temps.length; i++ ) {
//					int temp_y = y_temps[ i ];
//					int temp_x;
//					if ( temp_y < height && ( temp_x = y + x_temps[i] ) < width && temp_x > 0 ) {
//						int[] buffer3 = dither_buffer[ temp_y ];
//						float scalar = mult[ i ];
//						index = temp_x + ( temp_x << 1 );
//						buffer3[ index ] = ( int ) ( buffer3[index++] + scalar * delta_r );
//						buffer3[ index ] = ( int ) ( buffer3[index++] + scalar * delta_g );
//						buffer3[ index ] = ( int ) ( buffer3[index  ] + scalar * delta_b );
//					}
//				}
//				if ( ( rgb >> 24 & 0xFF ) < 0x80 ) {
//					map[ y * width + x ] = 0;
//				} else {
//					map[ y * width + x ] = COLOR_MAP[ red >> 1 << 14 | green >> 1 << 7 | blue >> 1 ];
//				}
//			}
//		}
//		return map;
//	}
	
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
	
	public static byte[] rotate( byte[] original, int width, byte[] copy, int copyWidth, double radians ) {
		int height = original.length / width;
		int copyHeight = copy.length / copyWidth;
		
		double cos = Math.cos( radians );
		double sin = Math.sin( radians );
		
		double xo = ( width - 1 ) * .5;
		double yo = ( height - 1 ) * .5;
		
		double copyXHalf = ( copyWidth - 1 ) * .5;
		double copyYHalf = ( copyHeight - 1 ) * .5;
		for ( int y = 0; y < copyHeight; y++ ) {
			double b = y - copyYHalf;
			int yHeight = y * copyWidth;
			for ( int x = 0; x < copyWidth; x++ ) {
				double a = x - copyXHalf;

				int xx = ( int ) ( a * cos - b * sin + xo + .5 );
				int yy = ( int ) ( a * sin + b * cos + yo + .5 );

				if ( xx >= 0 && xx < width && yy >= 0 && yy < height ) {
					copy[ x + yHeight ] = original[ xx + yy * width ];
				}
			}
		}

		return copy;
	}
	
	/**
	 * Takes in 2 colors and sets one as the foreground
	 * 
	 * @param baseColor
	 * The background color
	 * @param overlay
	 * The foreground
	 * @return
	 * The colors combined, if the foreground is not opaque
	 */
	public static int overwriteColor( int baseColor, int overlay ) {
		int a2 = overlay >>> 24 & 0xFF;
		if ( a2 == 0 ) {
			return baseColor;
		} else if ( a2 == 0xFF ) {
			return overlay;
		}
		int r2 = overlay >>> 16 & 0xFF;
	    int g2 = overlay >>> 8  & 0xFF;
	    int b2 = overlay        & 0xFF;
		
	    int a1 = Math.max( baseColor >>> 24 & 0xFF, a2 );
		int r1 = baseColor >>> 16 & 0xFF;
	    int g1 = baseColor >>> 8  & 0xFF;
	    int b1 = baseColor        & 0xFF;
	    
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
	
	public static int mediateARGB( int c1, int c2 ) {
	    int a1 = c1 & 0xFF000000 >>> 24;
	    int r1 = c1 & 0x00FF0000 >> 16;
	    int g1 = c1 & 0x0000FF00 >> 8;
	    int b1 = c1 & 0x000000FF ;

	    int a2 = c2 & 0xFF000000 >>> 24;
	    int r2 = c2 & 0x00FF0000 >> 16;
	    int g2 = c2 & 0x0000FF00 >> 8;
	    int b2 = c2 & 0x000000FF ;

	    int am = (a1 + a2) / 2;
	    int rm = (r1 + r2) / 2;
	    int gm = (g1 + g2) / 2;
	    int bm = (b1 + b2) / 2;

	    int m = am << 24 | rm << 16 | gm << 8 | bm; 


	    return m;
	}
	
	/**
	 * Brightens a given color for a percent; Negative values darken the color.
	 * 
	 * @param c
	 * The color to brighten.
	 * @param percent
	 * The percentage to brighten; Must not exceed 100 percent.
	 * @return
	 * The new brightened color.
	 */
	public static Color brightenColor( Color c, int percent ) {
		if ( percent == 0 ) return c;
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		if ( percent > 0 ) {
			int newr = r + percent * ( 255 - r ) / 100;
			int newg = g + percent * ( 255 - g ) / 100;
			int newb = b + percent * ( 255 - b ) / 100;
			return new Color( newr, newg, newb );
		}
		int newr = r + percent * r / 100;
		int newg = g + percent * g / 100;
		int newb = b + percent * b / 100;
		return new Color( newr, newg, newb, c.getAlpha() );
	}
}

class LoadRed extends RecursiveTask< byte[] > {
	protected final int r;
	protected final int[] palette;
	
	protected LoadRed( int[] palette, int r ) {
		this.r = r;
		this.palette = palette;
	}
	
	@Override
	protected byte[] compute() {
		List< LoadGreen > greenSub = new ArrayList< LoadGreen >( 128 );
		for ( int g = 0; g < 256; g += 2 ) {
			LoadGreen green = new LoadGreen( palette, r, g );
			
			greenSub.add( green );
			
			green.fork();
		}
		
		byte[] vals = new byte[ 16384 ];
		for ( int i = 0; i < 128; i++ ) {
			byte[] sub = greenSub.get( i ).join();
			int index = i << 7;
			for ( int si = 0; si < 128; si++ ) {
				vals[ index + si ] = sub[ si ];
			}
		}
		
		return vals;
	}
	
}

class LoadGreen extends RecursiveTask< byte[] > {
	protected final int r;
	protected final int g;
	protected final int[] palette;
	
	protected LoadGreen( int[] palette, int r, int g ) {
		this.r = r;
		this.g = g;
		this.palette = palette;
	}
	
	@Override
	protected byte[] compute() {
		List< LoadBlue > blueSub = new ArrayList< LoadBlue >( 128 );
		for ( int b = 0; b < 256; b += 2 ) {
			LoadBlue blue = new LoadBlue( palette, r, g, b );
			
			blueSub.add( blue );
			
			blue.fork();
		}
		
		byte[] matches = new byte[ 128 ];
		for ( int i = 0; i < 128; i++ ) {
			matches[ i ] = blueSub.get( i ).join();
		}
		
		return matches;
	}
}

class LoadBlue extends RecursiveTask< Byte > {
	protected final int r, g, b;
	protected final int[] palette;
	
	protected LoadBlue( int[] palette, int r, int g, int b ) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.palette = palette;
	}
	
	@Override
	protected Byte compute() {
		int val = 0;
		float best_distance = Float.MAX_VALUE;
		float distance = 0;
		int col = 0;
		for (int i = 4; i < palette.length; ++i) {
			col = palette[ i ];
			int r2 = col >> 16 & 0xFF;
			int g2 = col >> 8 & 0xFF;
			int b2 = col & 0xFF;
	
			float red_avg = ( r + r2 ) * .5f;
			int redVal = r - r2;
			int greenVal = g - g2;
			int blueVal = b - b2;
			float weight_red = 2.0f + red_avg * ( 1f / 256f );
			float weight_green = 4.0f;
			float weight_blue = 2.0f + ( 255.0f - red_avg ) * ( 1f / 256f );
			distance = weight_red * redVal * redVal + weight_green * greenVal * greenVal + weight_blue * blueVal * blueVal;
	
			if (distance < best_distance) {
				best_distance = distance;
				val = i;
			}
		}
		
		return ( byte ) val;
	}
}