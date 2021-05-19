package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.BitSet;
import java.util.concurrent.RecursiveTask;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
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
		double radians = info.setting.rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0;
		double cos = RivenMath.cos( ( float ) radians );
		double sin = RivenMath.sin( ( float ) radians );
		double oriX = info.setting.location.getX();
		double oriZ = info.setting.location.getZ();
		
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
		
		// Get the actual data, and background color
		for ( int i = 0; i < length; i++ ) {
			if ( !bitset.get( i ) ) {
				subIndex = i + index;
				
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

				byte chunkColor = info.cache.getStorage().getColorAt( new Location( loc.getWorld(), xVal, 0, zVal ), info.setting.getScale() );
				// Check if chunkColor is -1 or something
				
				int localColor = 0;
				if ( chunkColor != -1 ) {
					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkColor );
				} else {
					int chunkX = blockX >> 4;
					int chunkZ = blockZ >> 4;
					
					ChunkLocation cLocation = new ChunkLocation( loc.getWorld(), chunkX, chunkZ );
					subRenderInfo.requiresRender.add( new BigChunkLocation( cLocation ) );

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
