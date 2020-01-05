package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class SubRenderTask extends RecursiveTask<SubRenderInfo> {
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
			Location renderLoc = info.locations[ subIndex ];
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
				if ( info.cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
					subRenderInfo.requiresRender.add( new BigChunkLocation( cLocation ) );
				}

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
		return subRenderInfo;
	}
}
