package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.BitSet;
import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.RivenMath;

public class DataSubRenderTask extends RecursiveTask< SubRenderInfo > {
	protected RenderInfo info;
	protected int index;
	protected int length;

	protected DataSubRenderTask( RenderInfo info, int index, int length ) {
		this.info = info;
		this.index = index;
		this.length = length;
	}

	@Override
	protected SubRenderInfo compute() {
		SubRenderInfo subRenderInfo = new SubRenderInfo();
		BitSet bitset = new BitSet( length );
		int[] rawData = new int[ length ];
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
		
		int subIndex;
		
		// Get the highest pixels
		for ( int i = 0; i < length; i++ ) {
			if ( !bitset.get( i ) ) {
				subIndex = i + index;
				
				// Get the custom map pixel
				int mapColor = info.upperPixelInfo[ subIndex ];
				rawData[ i ] = mapColor;
				// If the pixel is opaque, then set the bit to true
				bitset.set( i, mapColor >>> 24 == 0xFF );
			}
		}
		
		// Get the overlay
		for ( int i = 0; i < length; i++ ) {
			if ( !bitset.get( i ) ) {
				subIndex = i + index;
				
				// Get the custom map pixel
				int mapColor = JetpImageUtil.overwriteColor( info.globalOverlay[ subIndex ], rawData[ i ] );
				rawData[ i ] = mapColor;
				// If the pixel is opaque, then set the bit to true
				bitset.set( i, mapColor >>> 24 == 0xFF );
			}
		}
		
		// Get the lower pixels
		for ( int i = 0; i < length; i++ ) {
			if ( !bitset.get( i ) ) {
				subIndex = i + index;
				
				// Get the custom map pixel
				int mapColor = JetpImageUtil.overwriteColor( info.lowerPixelInfo[ subIndex ], rawData[ i ] );
				rawData[ i ] = mapColor;
				// If the pixel is opaque, then set the bit to true
				bitset.set( i, mapColor >>> 24 == 0xFF );
			}
		}
		
		String world = loc.getWorld().getName();
		ChunkData lastChunkData = null;
		int lastChunkX = 0;
		int lastChunkZ = 0;
		
		// Get the actual data, and background color
		for ( int i = 0; i < length; i++ ) {
			if ( !bitset.get( i ) ) {
				subIndex = i + index;
				
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
				final int chunkX = blockX >> 4;
				final int chunkZ = blockZ >> 4;
				
				ChunkData chunkData = lastChunkData;
				if ( chunkData == null || chunkX != lastChunkX || chunkZ != lastChunkZ ) {
					chunkData = info.cache.getDataAt( new ChunkLocation( world, chunkX, chunkZ ) );
				}
				
				int localColor = 0;
				if ( chunkData != null ) {
					lastChunkData = chunkData;
					lastChunkX = chunkX;
					lastChunkZ = chunkZ;
					
					final int xOffset = blockX & 0xF;
					final int zOffset = blockZ & 0xF;
					
					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset ) );
				} else {
					subRenderInfo.requiresRender.add( new BigChunkLocation( world, chunkX >> 4, chunkZ >> 4 ) );

					localColor = loading;
				}

				// First, insert any WorldPixels that may be present
				for ( WorldPixel pixel : info.worldPixels ) {
					if ( pixel.intersects( xVal, zVal ) ) {
						localColor = JetpImageUtil.overwriteColor( localColor, pixel.getColor().getRGB() );
					}
				}

				// Then, get the color and mix it under the current overlay color
				int mapColor = rawData[ i ];
				mapColor = JetpImageUtil.overwriteColor( localColor, mapColor );
				mapColor = JetpImageUtil.overwriteColor( loading, mapColor );
				rawData[ i ] = mapColor;
			}
		}
		
		for ( int i = 0; i < length; i++ ) {
			data[ i ] = JetpImageUtil.getBestColorIncludingTransparent( rawData[ i ] );
		}
		
		return subRenderInfo;
	}
}
