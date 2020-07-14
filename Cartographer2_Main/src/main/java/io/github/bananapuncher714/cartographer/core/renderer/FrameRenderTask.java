package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.bukkit.Location;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.util.IcecoreMath;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.github.bananapuncher714.cartographer.core.util.RivenMath;

public class FrameRenderTask extends RecursiveAction {
	// There are 128 * 128 pixels that need to be processed
	// Keep in mind cache lines? Don't want to waste time
	private static final int SUBTASK_INTERVAL = 128;
	private static final int CANVAS_SIZE = 128 * 128;
	
	protected RenderInfo info;
	
	protected FrameRenderTask( RenderInfo info ) {
		this.info = info;
	}
	
	@Override
	protected void compute() {
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
				int index = x + y * 128;
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
		
		// Construct the fork join pools required for the interval below and run
		List< SubRenderTask > tasks = new ArrayList< SubRenderTask >();
		for ( int subTaskIndex = 0; subTaskIndex < 128 * 128; subTaskIndex += SUBTASK_INTERVAL ) {
			SubRenderTask task = new SubRenderTask( info, subTaskIndex, SUBTASK_INTERVAL );
			tasks.add( task );
			task.fork();
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
		
		// Once they have been run, join them together
		for ( SubRenderTask task : tasks ) {
			SubRenderInfo subInfo = task.join();

			info.needsRender.addAll( subInfo.requiresRender );
			for ( int i = 0; i < SUBTASK_INTERVAL; i++ ) {
				data[ i + subInfo.index ] = subInfo.data[ i ];
			}
		}
	}
}
