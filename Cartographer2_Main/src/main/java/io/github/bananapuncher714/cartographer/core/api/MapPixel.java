package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

import org.apache.commons.lang.Validate;

/**
 * Represents a pixel on the map canvas.
 * 
 * @author BananaPuncher714
 */
public class MapPixel implements Comparable< MapPixel > {
	protected int x, z;
	protected int priority;
	protected Color color;
	
	/**
	 * Construct a MapPixel with the arguments provided.
	 * 
	 * @param x
	 * The x coordinate, from 0 to 127.
	 * @param z
	 * The z coordinate, from 0 to 127.
	 * @param color
	 * An ARGB color, cannot be null.
	 */
	public MapPixel( int x, int z, Color color ) {
		Validate.notNull( color );
		this.x = x;
		this.z = z;
		this.color = color;
	}

	/**
	 * Get the x coordinate.
	 * 
	 * @return
	 * The x coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the z coordinate.
	 * 
	 * @return
	 * The z coordinate.
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Get the color.
	 * 
	 * @return
	 * An ARGB color.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Get the priority of this pixel. 0 to 0xFFFE means it will appear under the overlay. Higher than that means it appears above.
	 * 
	 * @return
	 * The priority of the pixel.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Set the priority of this pixel. 0 to 0xFFFE means it will appear under the overlay. Higher than that means it appears above.
	 * 
	 * @param priority
	 * The priority of this pixel.
	 */
	public void setPriority( int priority ) {
		this.priority = priority;
	}

	@Override
	public int compareTo( MapPixel pixel ) {
		if ( priority > pixel.priority ) {
			return 1;
		} else {
			return -1;
		}
	}
}
