package io.github.bananapuncher714.cartographer.core.util;

import java.awt.Image;
import java.util.Set;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;

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
		return null;
	}
	
	public static Location getPixelToLocation( Location center, double scale, double radians, int[] pixel ) {
		return null;
	}
	
	/**
	 * Convert a Location to a relative point on the map
	 * 
	 * @param center
	 * @param point
	 * @param scale
	 * @param radians
	 * Amount of radians to rotate by. Add pi or 180 degrees if using a player's yaw.
	 * @return
	 */
	public static int[] getLocationToPixel( Location center, Location point, double scale, double radians ) {
		return null;
	}
	
	public static byte getDirection( double degree ) {
		return 0;
	}
	
	//TODO add dithering in some way?
	public static Set< MapPixel > getPixelsFor( Image image, int x, int y ) {
		return null;
	}
}
