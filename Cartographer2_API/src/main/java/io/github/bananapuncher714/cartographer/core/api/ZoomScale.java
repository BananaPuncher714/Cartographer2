package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.map.MapView.Scale;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Zoom scales for the map renderer. More scales may be added in the future.
 * TODO Make this not an enum, and let maps freely assign whatever scales they want
 * 
 * @author BananaPuncher714
 * @deprecated
 */
public enum ZoomScale {
	SIXTY_FOURTH( .015625 ),
	THIRTY_SECONDTH( .03125 ),
	SIXTEENTH( .0625 ),
	EIGHTH( .125 ),
	FOURTH( .25 ),
	HALF( .5 ),
	ONE( 1 ),
	TWO( 2 ),
	FOUR( 4 ),
	EIGHT( 8 ),
	SIXTEEN( 16 );
	
	private final double blocks;
	
	private ZoomScale( double bpp ) {
		blocks = bpp;
	}
	
	/**
	 * The amount of blocks per pixel that would be shown on the {@link Minimap}.
	 * 
	 * @return
	 * The amount of blocks that would be required to equal the width of a pixel.
	 */
	public double getBlocksPerPixel() {
		return blocks;
	}
	
	/**
	 * Get the previous ZoomScale.
	 * 
	 * @param circular
	 * Whether or not to wrap around from the end of the ZoomScale list.
	 * @return
	 * Will return the same ZoomScale if not circular and has reached the end of the list.
	 */
	public ZoomScale zoom( boolean circular ) {
		for ( int index = 0; index < ZoomScale.values().length; index++ ) {
			ZoomScale scale = ZoomScale.values()[ index ];
			if ( blocks == scale.blocks ) {
				return ZoomScale.values()[ ( circular && index == 0 ) ? ZoomScale.values().length - 1 : Math.max( 0, index - 1 ) ];
			}
		}
		return null;
	}
	
	/**
	 * Get the next ZoomScale.
	 * 
	 * @param circular
	 * Whether or not to wrap around from the beginning of the ZoomScale list.
	 * @return
	 * Will return the same ZoomScale if not circular and has reached the end of the list.
	 */
	public ZoomScale unzoom( boolean circular ) {
		for ( int index = 0; index < ZoomScale.values().length; index++ ) {
			ZoomScale scale = ZoomScale.values()[ index ];
			if ( blocks == scale.blocks ) {
				return ZoomScale.values()[ ( circular && index == ZoomScale.values().length - 1 ) ? 0 : Math.min( ZoomScale.values().length - 1, index + 1 ) ];
			}
		}
		return null;
	}
	
	
	/**
	 * Get the closest ZoomScale to the value provided.
	 * 
	 * @param scale
	 * Scale in blocks-per-pixel.
	 * @return
	 * The closest ZoomScale, may be larger or lower than the scale provided.
	 */
	public static ZoomScale getScale( double scale ) {
		double closest = Double.MAX_VALUE;
		ZoomScale zs = null;
		for ( ZoomScale possibleScale : values() ) {
			double dist = Math.abs( possibleScale.blocks - scale );
			if ( dist < closest ) {
				zs = possibleScale;
				closest = dist;
			}
		}
		return zs;
	}

	/**
	 * Check if the zoom scale is the most zoomed out.
	 * 
	 * @return
	 * If the blocks per pixel is the largest value.
	 */
	public boolean isLeastZoomed() {
		return this == ZoomScale.values()[ ZoomScale.values().length - 1 ];
	}
	
	/**
	 * Check if the zoom scale is the most zoomed in.
	 * 
	 * @return
	 * If the blocks per pixel is smallest value.
	 */
	public boolean isMostZoomed() {
		return this == ZoomScale.values()[ 0 ]; 
	}
	
	/**
	 * Convert Bukkit's map scaling to a zoom scale.
	 * 
	 * @param scale
	 * Minecraft's default map scaling
	 * @return
	 * Returns ZoomScale.ONE by default.
	 */
	public static ZoomScale getScaleFromBukkit( Scale scale ) {
		switch( scale ) {
		case CLOSEST: return ZoomScale.ONE;
		case CLOSE: return ZoomScale.TWO;
		case NORMAL: return ZoomScale.FOUR;
		case FAR: return ZoomScale.EIGHT;
		case FARTHEST: return ZoomScale.SIXTEEN;
		default: return ZoomScale.ONE;
		}
	}
}
