package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.map.MapView.Scale;

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
	
	public double getBlocksPerPixel() {
		return blocks;
	}
	
	public ZoomScale getLower( boolean circular ) {
		for ( int index = 0; index < ZoomScale.values().length; index++ ) {
			ZoomScale scale = ZoomScale.values()[ index ];
			if ( name().equalsIgnoreCase( scale.name() ) ) {
				return ZoomScale.values()[ ( circular && index == 0 ) ? ZoomScale.values().length - 1 : Math.max( 0, index - 1 ) ];
			}
		}
		return null;
	}
	
	public ZoomScale getHigher( boolean circular ) {
		for ( int index = 0; index < ZoomScale.values().length; index++ ) {
			ZoomScale scale = ZoomScale.values()[ index ];
			if ( name().equalsIgnoreCase( scale.name() ) ) {
				return ZoomScale.values()[ ( circular && index == ZoomScale.values().length - 1 ) ? 0 : Math.min( ZoomScale.values().length - 1, index + 1 ) ];
			}
		}
		return null;
	}
	
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
