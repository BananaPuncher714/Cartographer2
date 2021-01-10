package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
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
		double radians = info.setting.rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0;
		double cos = RivenMath.cos( ( float ) radians );
		double sin = RivenMath.sin( ( float ) radians );
		double oriX = info.setting.location.getX();
		double oriZ = info.setting.location.getZ();
		
		for ( int i = 0; i < length; i++ ) {
			int subIndex = i + index;
			
			int mapColor = 0;

			// Get the custom map pixel
			int color = info.upperPixelInfo[ subIndex ];
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
			double a = ( subIndex & 127 ) - 64;
			double b = ( subIndex >> 7 ) - 64;
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
					subRenderInfo.requiresRender.add( new BigChunkLocation( cLocation ) );
//				}

				localColor = loading;
			}

			// First, insert any WorldPixels that may be present
			for ( WorldPixel pixel : info.worldPixels ) {
				if ( pixel.getWorld() == loc.getWorld() && pixel.intersects( xVal, zVal ) ) {
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
