package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

import org.bukkit.Location;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.util.IcecoreMath;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.github.bananapuncher714.cartographer.core.util.RivenMath;

public class FrameRenderTask extends RecursiveAction {
	protected RenderInfo info;
	
	public FrameRenderTask( RenderInfo info ) {
		this.info = info;
	}
	
	@Override
	protected void compute() {
		// Set up the arrays needed
		byte[] data = new byte[ 128 * 128 ];
		int[] higherMapPixels = new int[ 128 * 128 ];
		int[] lowerMapPixels = new int[ 128 * 128 ];
		int[] globalOverlay = Cartographer.getInstance().getOverlay().getImage();
		int[] loadingBackground = Cartographer.getInstance().getLoadingImage().getImage();
		
		// Set the information to the render info
		info.data = data;
		info.upperPixelInfo = higherMapPixels;
		info.lowerPixelInfo = lowerMapPixels;
		info.globalOverlay = globalOverlay;
		info.background = loadingBackground;
		
		// Get the locations around that need rendering
		Location loc = info.setting.location;
		BooleanOption rotation = info.map.getSettings().getRotation();
		boolean rotating = rotation == BooleanOption.DEFAULT ? info.setting.rotating : ( rotation == BooleanOption.ON ? true : false );
		Location[] locations = MapUtil.getLocationsAround( loc, info.setting.zoomscale, rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0 );
		
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

				if ( pixel.getDepth() < 0xFFFF ) {
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
		int interval = 128;
		List< SubRenderTask > tasks = new ArrayList< SubRenderTask >();
		for ( int subTaskIndex = 0; subTaskIndex < 128 * 128; subTaskIndex += interval ) {
			SubRenderTask task = new SubRenderTask( info, subTaskIndex, interval );
			tasks.add( task );
			task.fork();
		}
		
		// Calculate the cursor info while the sub render tasks are running
		double yawOffset = rotating ? loc.getYaw() + 180 : 0;
		
		MapCursor[] cursors = null;
		cursors = new MapCursor[ info.worldCursors.size() + info.mapCursors.size() ];
		int index = 0;
		for ( WorldCursor cursor : info.worldCursors ) {
			Location cursorLoc = cursor.getLocation();
			double yaw = cursorLoc.getYaw() - yawOffset + 720;
			double relX = cursorLoc.getX() - loc.getX();
			double relZ = cursorLoc.getZ() - loc.getZ();
			double distance = Math.sqrt( relX * relX + relZ * relZ );

			double radians = IcecoreMath.atan2_Op_2( ( float ) relZ, ( float ) relX ) - Math.toRadians( yawOffset );
			double newRelX = 2 * distance * RivenMath.cos( ( float ) radians );
			double newRelZ = 2 * distance * RivenMath.sin( ( float ) radians );

			int normalizedX = ( int ) Math.min( 127, Math.max( -127, newRelX / info.setting.zoomscale ) );
			int normalizedZ = ( int ) Math.min( 127, Math.max( -127, newRelZ / info.setting.zoomscale ) );

			cursors[ index++ ] = Cartographer.getInstance().getHandler().constructMapCursor( normalizedX, normalizedZ, yaw, cursor.getType(), cursor.getName() );
		}
		for ( MapCursor cursor : info.mapCursors ) {
			cursors[ index++ ] = cursor;
		}
		
		// Once they have been run, join them together
		for ( SubRenderTask task : tasks ) {
			SubRenderInfo subInfo = task.join();

			info.needsRender.addAll( subInfo.requiresRender );
			for ( int i = 0; i < interval; i++ ) {
				data[ i + subInfo.index ] = subInfo.data[ i ];
			}
		}
	}
}
