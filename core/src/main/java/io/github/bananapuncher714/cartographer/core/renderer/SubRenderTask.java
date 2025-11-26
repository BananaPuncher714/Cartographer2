package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.RivenMath;

public class SubRenderTask extends RecursiveTask< SubRenderInfo > {
	protected RenderInfo info;
	protected int index;
	protected int length;

	protected SubRenderTask( RenderInfo info, int index, int length ) {
		this.info = info;
		this.index = index;
		this.length = length;
	}

	@Override
	protected SubRenderInfo compute() {
		SubRenderInfo subRenderInfo = new SubRenderInfo();
		byte[] data = new byte[ length ];
		subRenderInfo.data = data;
		subRenderInfo.index = index;
		
		// Calculate the information for the rotations and whatever we can right now
		Location loc = info.setting.location;
		final double radians = info.setting.rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0;
		final double cos = RivenMath.cos( ( float ) radians );
		final double sin = RivenMath.sin( ( float ) radians );
		final double oriX = info.setting.location.getX();
		final double oriZ = info.setting.location.getZ();
		
		for ( int i = 0; i < length; i++ ) {
			int subIndex = i + index;
			
			// Get the custom map pixel
			int mapColor = info.upperPixelInfo[ subIndex ];
			// Continue if the pixel is opaque, since we know that nothing else be above this
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			}

			// Then the global overlay
			// The global overlay is still the background to the foreground
			mapColor = JetpImageUtil.overwriteColor( info.globalOverlay[ subIndex ], mapColor );

			// See if the global overlay is opaque
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			}

			int lowerColor = info.lowerPixelInfo[ subIndex ];
			mapColor = JetpImageUtil.overwriteColor( lowerColor, mapColor );

			// See if the pixels are opaque
			if ( mapColor >>> 24 == 0xFF ) {
				data[ i ] = JetpImageUtil.getBestColor( mapColor );
				continue;
			}

			// Then get the loading background
			int loading = info.background[ subIndex ];

			// The render location comes next
			// Calculate the x and y manually
			final double a = ( subIndex & 127 ) - 64;
			final double b = ( subIndex >> 7 ) - 64;
			final double xx = a * cos - b * sin;
			final double yy = a * sin + b * cos;
			final double xVal = oriX + ( info.setting.zoomscale * xx );
			final double zVal = oriZ + ( info.setting.zoomscale * yy );
			final int blockX = ( int ) Math.floor( xVal );
			final int blockZ = ( int ) Math.floor( zVal );

			byte chunkColor = info.cache.getStorage().getColorAt( new Location( loc.getWorld(), xVal, 0, zVal ), info.setting.getScale() );
			// Check if chunkColor is -1 or something
			
			int localColor = 0;
			if ( chunkColor != -1 ) {
				localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkColor );
			} else {
				int chunkX = blockX >> 4;
				int chunkZ = blockZ >> 4;
				
				ChunkLocation cLocation = new ChunkLocation( loc.getWorld(), chunkX, chunkZ );
				// Don't check if it requires generation, or if the chunk is being loaded here
				// It should be done somewhere else
				// Just add it to the collection and check it later
//				if ( info.cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
					subRenderInfo.requiresRender.add( new BigChunkLocation( cLocation ) );
//				}

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

			data[ i ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
		}
		return subRenderInfo;
	}
}
