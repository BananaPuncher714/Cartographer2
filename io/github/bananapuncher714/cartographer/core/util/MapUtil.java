package io.github.bananapuncher714.cartographer.core.util;

import java.awt.Color;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;

import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

public class MapUtil {
	/**
	 * Create an array of locations for a map
	 * 
	 * @param center
	 * The center of the map
	 * @param scale
	 * The scale in blocks per pixel
	 * @param radians
	 * The amount of radians to rotate
	 * @return
	 * An array the size of 128*128
	 */
	public static Location[] getLocationsAround( Location center, double scale, double radians ) {
		Location[] locations = new Location[ 128 * 128 ];
		
		int length = 128;
		
		double cos = Math.cos( radians );
		double sin = Math.sin( radians );
		
		double side = length * .5;
		
		for ( int y = 0; y < length; y++ ) {
			double b = y - side;
			int yHeight = y * length;
			for ( int x = 0; x < length; x++ ) {
				double a = x - side;

				double xx = a * cos - b * sin;
				double yy = a * sin + b * cos;

				locations[ x + yHeight ] = center.clone().add( scale * xx, 0, scale * yy );
			}
		}
		
		return locations;
	}
	
	public static byte getDirection( double degree ) {
		return ( byte ) Math.min( 15, Math.max( 0, ( ( ( degree + 371.25 ) % 360 ) / 22.5 ) ) );
	}
	
	public static byte getColorAt( Location location, MinimapPalette palette ) {
		int height = BlockUtil.getHighestYAt( location, palette.getTransparentBlocks() );
		int prevVal = BlockUtil.getHighestYAt( location.clone().subtract( 0, 0, 1 ), palette.getTransparentBlocks() );
		Location highest = location.clone();
		highest.setY( height );
		Material material = highest.getBlock().getType();
		Color color = palette.getColor( material );
		if ( prevVal > 0 ) {
			if ( prevVal == height ) {
				color = JetpImageUtil.brightenColor( color, -10 );
			} else if ( prevVal > height ) {
				color = JetpImageUtil.brightenColor( color, -30 );
			}
		}
		
		return JetpImageUtil.getBestColor( color.getRGB() );
	}
}
