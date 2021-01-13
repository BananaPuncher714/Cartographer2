package io.github.bananapuncher714.cartographer.core.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.util.IcecoreMath;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.RivenMath;

// Test rendering the full thing with one thread
public class FullRenderTask extends RecursiveTask< RenderInfo > {
	private static final int CANVAS_SIZE = 128 * 128;
	
	protected RenderInfo info;
	
	protected FullRenderTask( RenderInfo info ) {
		this.info = info;
	}
	
	@Override
	protected RenderInfo compute() {
		// Set up the arrays needed
		byte[] data = new byte[ CANVAS_SIZE ];
		int[] higherMapPixels = new int[ CANVAS_SIZE ];
		int[] lowerMapPixels = new int[ CANVAS_SIZE ];
		// Make sure it's not null
		int[] globalOverlay;
		if ( info.overlayImage != null ) {
			globalOverlay = info.overlayImage.getImage();
			if ( info.map.getSettings().isDitherOverlay() ) {
				globalOverlay = globalOverlay.clone();
				JetpImageUtil.dither( globalOverlay, 128 );
			}
		} else {
			globalOverlay = new int[ CANVAS_SIZE ];
		}
		
		int[] loadingBackground;
		if ( info.backgroundImage != null ) {
			loadingBackground = info.backgroundImage.getImage();
			if ( info.map.getSettings().isDitherBackground() ) {
				loadingBackground = loadingBackground.clone();
				JetpImageUtil.dither( loadingBackground, 128 );
			}
		} else {
			loadingBackground = new int[ CANVAS_SIZE ];
		}
		
		// Set the information to the render info
		info.data = data;
		info.upperPixelInfo = higherMapPixels;
		info.lowerPixelInfo = lowerMapPixels;
		info.globalOverlay = globalOverlay;
		info.background = loadingBackground;
		
		// Construct lowerMapPixels and higherMapPixels
		for ( Iterator< MapPixel > pixelIterator = info.mapPixels.iterator(); pixelIterator.hasNext(); ) {
			MapPixel pixel = pixelIterator.next();
			int x = pixel.getX();
			int y = pixel.getZ();
			if ( x < 128 && x >= 0 && y < 128 && y >= 0 ) {
				int index = x + ( y << 7 );
				int color = pixel.getColor().getRGB();
				if ( color >>> 24 == 0 ) {
					continue;
				}

				if ( pixel.getPriority() < 0xFFFF ) {
					int prevColor = lowerMapPixels[ index ];
					// These go under the overlay
					lowerMapPixels[ index ] = JetpImageUtil.overwriteColor( prevColor, color );
				} else {
					int prevColor = higherMapPixels[ index ];
					// Add the colors on top of the overlay. The pixels provided have priority
					higherMapPixels[ index ] = JetpImageUtil.overwriteColor( prevColor, color );
				}
			}
		}
		
		// Calculate the cursor info while the sub render tasks are running
		Location loc = info.setting.location;
		double yawOffset = info.setting.rotating ? loc.getYaw() + 180 : 0;
		
		List< MapCursor > cursorList = new ArrayList< MapCursor >( info.mapCursors );
		for ( WorldCursor cursor : info.worldCursors ) {
			Location cursorLoc = cursor.getLocation();
			double yaw = cursorLoc.getYaw() - yawOffset + 720;
			double relX = cursorLoc.getX() - loc.getX();
			double relZ = cursorLoc.getZ() - loc.getZ();
			double distance = Math.sqrt( relX * relX + relZ * relZ );

			double radians = IcecoreMath.atan2_Op_2( ( float ) relZ, ( float ) relX ) - Math.toRadians( yawOffset );
			double newRelX = 2 * distance * RivenMath.cos( ( float ) radians );
			double newRelZ = 2 * distance * RivenMath.sin( ( float ) radians );

			double scaledX = newRelX / info.setting.zoomscale;
			double scaledZ = newRelZ / info.setting.zoomscale;
			
			// The range may range from -127 to 127. Not sure about -128
			if ( cursor.isGlobal() ||
					( scaledX > -128 && scaledX < 128 &&
					  scaledZ > -128 && scaledZ < 128 ) ) {
				int normalizedX = ( int ) Math.min( 127, Math.max( -128, scaledX ) );
				int normalizedZ = ( int ) Math.min( 127, Math.max( -128, scaledZ ) );
				
				cursorList.add( Cartographer.getInstance().getHandler().constructMapCursor( normalizedX, normalizedZ, yaw, cursor.getType(), cursor.getName() ) );
			}
		}
		info.cursors = cursorList.toArray( new MapCursor[ cursorList.size() ] );
		
		// Trim any redundant world pixels
		double rad = info.setting.zoomscale * 91;
		WorldPixel global = new WorldPixel( loc.getWorld(), loc.getX() - rad, loc.getZ() - rad, Color.BLACK );
		global.setHeight( rad * 2 );
		global.setWidth( rad * 2 );
		for ( Iterator< WorldPixel > it = info.worldPixels.iterator(); it.hasNext(); ) {
			WorldPixel pixel = it.next();
			if ( pixel.getWorld() != loc.getWorld() || !global.intersects( pixel ) ) {
				it.remove();
			}
		}
		
		// Calculate the information for the rotations and whatever we can right now
		double radians = info.setting.rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0;
		double cos = RivenMath.cos( ( float ) radians );
		double sin = RivenMath.sin( ( float ) radians );
		double oriX = info.setting.location.getX();
		double oriZ = info.setting.location.getZ();
		
		int index = -1;
		for ( int y = 0; y < 128; y++ ) {
			double b = y - 64;
			for ( int x = 0; x < 128; x++ ) {
				double a = x - 64;
				
				index++;

				// Get the custom map pixel
				int mapColor = info.upperPixelInfo[ index ];
				// Continue if the pixel is opaque, since we know that nothing else be above this
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColor( mapColor );
					continue;
				}

				// Then the global overlay
				// The global overlay is still the background to the foreground
				mapColor = JetpImageUtil.overwriteColor( info.globalOverlay[ index ], mapColor );

				// See if the global overlay is opaque
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColor( mapColor );
					continue;
				}

				int lowerColor = info.lowerPixelInfo[ index ];
				mapColor = JetpImageUtil.overwriteColor( lowerColor, mapColor );

				// See if the pixels are opaque
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColor( mapColor );
					continue;
				}

				// Then get the loading background
				int loading = info.background[ index ];

				// The render location comes next
				// Calculate the x and y manually
				double xx = a * cos - b * sin;
				double yy = a * sin + b * cos;
				double xVal = oriX + ( info.setting.zoomscale * xx );
				double zVal = oriZ + ( info.setting.zoomscale * yy );
				int blockX = ( int ) xVal;
				int blockZ = ( int ) zVal;
				int chunkX = blockX >> 4;
				int chunkZ = blockZ >> 4;
				
				ChunkLocation cLocation = new ChunkLocation( loc.getWorld(), chunkX, chunkZ );
				int xOffset = blockX & 0xF;
				int zOffset = blockZ & 0xF;
				int localColor = 0;

				ChunkData chunkData = info.cache.getDataAt( cLocation );

				if ( chunkData != null ) {
					// TODO make this configurable per player or something. Make a player preference thing or whatnot.
					// This is for static colors
//	  				localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset, setting.getScale() ) );
					// This is for dynamic colors
					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset ) );
				} else {
					info.needsRender.add( new BigChunkLocation( cLocation ) );

					localColor = loading;
				}

				// First, insert any WorldPixels that may be present
				for ( WorldPixel pixel : info.worldPixels ) {
					if ( pixel.intersects( xVal, zVal ) ) {
						localColor = JetpImageUtil.overwriteColor( localColor, pixel.getColor().getRGB() );
					}
				}

				// Then, get the color and mix it under the current overlay color
				mapColor = JetpImageUtil.overwriteColor( localColor, mapColor );
				mapColor = JetpImageUtil.overwriteColor( loading, mapColor );

				data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
			}
		}
		
		return info;
	}
}
