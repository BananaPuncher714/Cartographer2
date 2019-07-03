package io.github.bananapuncher714.cartographer.core.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
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
		CrossVersionMaterial material = Cartographer.getInstance().getHandler().getUtil().getBlockType( highest.getBlock() );
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
	
	public static Collection< MapPixel > getPixelsFor( Image image, int x, int y ) {
		BufferedImage bImage = JetpImageUtil.toBufferedImage( image );
		int width = bImage.getWidth();
		int[] data = JetpImageUtil.getRGBArray( bImage );
		Set< MapPixel > pixels = new HashSet< MapPixel >();
		for ( int i = 0; i < data.length; i++ ) {
			int argb = data[ i ];
			if ( argb >>> 24 > 0 ) {
				int w = i % width;
				int h = i / width;
				int nx = w + x;
				int ny = h + y;
				if ( nx >=0 && nx < 128 && ny >=0 && ny < 128 ) {
					pixels.add( new MapPixel( nx, ny, new Color( argb ) ) );
				}
			}
		}
		return pixels;
	}
}
