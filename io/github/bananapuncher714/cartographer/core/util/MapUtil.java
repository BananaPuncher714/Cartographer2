package io.github.bananapuncher714.cartographer.core.util;

import org.bukkit.Location;

public class MapUtil {
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
}
