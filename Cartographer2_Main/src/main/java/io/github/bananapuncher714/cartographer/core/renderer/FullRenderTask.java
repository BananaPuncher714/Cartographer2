package io.github.bananapuncher714.cartographer.core.renderer;

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
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
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
		
		// Get the locations around that need rendering
		Location loc = info.setting.location;
		Location[] locations = MapUtil.getLocationsAround( loc, info.setting.zoomscale, info.setting.rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0 );
		
		// Set the locations
		info.locations = locations;
		
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
		
		for ( int i = 0; i < CANVAS_SIZE; i++ ) {
			int mapColor = 0;

			// Get the custom map pixel
			int color = info.upperPixelInfo[ i ];
			// Continue if the pixel is opaque, since we know that nothing else be above this
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			} else {
				// Otherwise, we want to set it as the bottom layer
				mapColor = color;
			}

			// Then the global overlay
			// The global overlay is still the background to the foreground
			mapColor = JetpImageUtil.overwriteColor( info.globalOverlay[ i ], mapColor );

			// See if the global overlay is opaque
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			}

			int lowerColor = info.lowerPixelInfo[ i ];
			mapColor = JetpImageUtil.overwriteColor( lowerColor, mapColor );

			// See if the pixels are opaque
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			}

			// Then get the loading background
			int loading = info.background[ i ];

			// The render location comes next
			// TODO Perhaps calculate this location here rather than creating an array of 128 * 128 locations?
			// Not sure how much faster this would be though
			Location renderLoc = info.locations[ i ];
			// If renderLoc is null, we know it doesn't exist
			// Therefore, overwrite it with whatever color mapColor is
			if ( renderLoc == null ) {
				data[ i ] = JetpImageUtil.getBestColorIncludingTransparent( JetpImageUtil.overwriteColor( loading, mapColor ) );
				continue;
			}

			// If not, then we try and get the render location
			ChunkLocation cLocation = new ChunkLocation( renderLoc );
			int xOffset = renderLoc.getBlockX() - ( cLocation.getX() << 4 );
			int zOffset = renderLoc.getBlockZ() - ( cLocation.getZ() << 4 );

			ChunkData chunkData = info.cache.getDataAt( cLocation );

			int localColor = 0;
			if ( chunkData != null ) {
				// TODO make this configurable per player or something. Make a player preference thing or whatnot.
				// This is for static colors
//  			localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset, setting.getScale() ) );
				// This is for dynamic colors
				localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset ) );
			} else {
				// Don't check if it requires generation, or if the chunk is being loaded here
				// It should be done somewhere else
				// Just add it to the collection and check it later
//				if ( info.cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
					info.needsRender.add( new BigChunkLocation( cLocation ) );
//				}

				localColor = loading;
			}

			// First, insert any WorldPixels that may be present
			for ( WorldPixel pixel : info.worldPixels ) {
				if ( renderLoc.getWorld() == info.setting.location.getWorld() && pixel.intersects( renderLoc.getX(), renderLoc.getZ() ) ) {
					localColor = JetpImageUtil.overwriteColor( localColor, pixel.getColor().getRGB() );
				}
			}

			// Then, get the color and mix it under the current overlay color
			mapColor = JetpImageUtil.overwriteColor( localColor, mapColor );
			mapColor = JetpImageUtil.overwriteColor( loading, mapColor );

			data[ i ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
		}
		
		return info;
	}
}
