package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a region of the Minecraft world to shade.
 * 
 * @author BananaPuncher714
 */
public class WorldPixel implements Comparable< WorldPixel > {
	/**
	 * The world that this pixel is in.
	 */
	protected World world;
	/**
	 * The minimum x and z coordinates of the region.
	 */
	protected double x, z;
	
	/**
	 * The priority ordering from 0 to 0xFFFFFF. Lower values mean they appear behind other pixels of greater priority.
	 */
	protected double priority;
	private double centerX, centerY, radX, radY;
	private double width = 1, height = 1;

	/**
	 * The location of the minimum corner, with the priority representing the y value.
	 */
	protected Location location;
	
	/**
	 * An ARGB color representing the color of this region.
	 */
	protected Color color;
	
	/**
	 * Construct a WorldPixel with the color and location provided.
	 * 
	 * @param location
	 * The y value will be used as the priority. Cannot be null.
	 * @param color
	 * The color of the region, cannot be null.
	 */
	public WorldPixel( Location location, Color color ) {
		Validate.notNull( location );
		Validate.notNull( color );
		this.location = location.clone();
		this.world = location.getWorld();
		this.x = location.getBlockX();
		this.priority = location.getBlockY();
		this.z = location.getBlockZ();
		this.color = color;
		calculateDimensions();
	}
	
	/**
	 * Construct a WorldPixel with the arguments provided.
	 * 
	 * @param world
	 * The world of the pixel, cannot be null.
	 * @param x
	 * The x coordinate of the pixel.
	 * @param z
	 * The z coordinate of the pixel.
	 * @param color
	 * The color of the region, cannot be null.
	 */
	public WorldPixel( World world, double x, double z, Color color ) {
		Validate.notNull( world );
		Validate.notNull( color );
		this.location = new Location( world, x, priority, z );
		this.world = world;
		this.x = x;
		this.z = z;
		this.color = color;
		calculateDimensions();
	}
	
	private void calculateDimensions() {
		radX = width / 2.0;
		radY = width / 2.0;
		centerX = x + radX;
		centerY = z + radY;
	}

	/**
	 * Check if this region intersects with the coordinates provided.
	 * 
	 * @param x
	 * The x coordinate.
	 * @param z
	 * The z coordinate.
	 * @return
	 * If the coordinate falls within or on the edge of this WorldPixel.
	 */
	public boolean intersects( double x, double z ) {
		return Math.abs( x - centerX ) <= radX && Math.abs( z - centerY ) <= radY;
	}
	
	/**
	 * Get the world.
	 * 
	 * @return
	 * Will not be null.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Set the world.
	 * 
	 * @param world
	 * Cannot be null.
	 */
	public void setWorld( World world ) {
		Validate.notNull( world );
		this.world = world;
	}

	/**
	 * Get the minimum x coordinate.
	 * 
	 * @return
	 * The x coordinate.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set the minimum x coordinate.
	 * 
	 * @param x
	 * The x coordinate.
	 */
	public void setX( double x ) {
		this.x = x;
		location.setX( x );
		calculateDimensions();
	}

	/**
	 * Get the width.
	 * 
	 * @return
	 * Should not be 0.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Set the width.
	 * 
	 * @param width
	 * Should not be 0.
	 */
	public void setWidth( double width ) {
		this.width = width;
		calculateDimensions();
	}

	/**
	 * Get the height.
	 * 
	 * @return
	 * Should not be 0.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Set the height.
	 * 
	 * @param height
	 * Should not be 0.
	 */
	public void setHeight( double height) {
		this.height = height;
		calculateDimensions();
	}

	/**
	 * Get the priority. Higher values means it will be drawn higher up than other pixels.
	 * 
	 * @return
	 * The priority.
	 */
	public double getPriority() {
		return priority;
	}

	/**
	 * Set the priority. Higher values means it will be drawn higher up than other pixels.
	 * 
	 * @param y
	 * The priority.
	 */
	public void setPriority( double y ) {
		this.priority = y;
		location.setY( y );
	}

	/**
	 * Get the minimum z coordinate.
	 * 
	 * @return
	 * The z coordinate.
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Set the minimum z coordinate.
	 * 
	 * @param z
	 * The z coordinate.
	 */
	public void setZ( double z ) {
		this.z = z;
		location.setZ( z );
		calculateDimensions();
	}

	/**
	 * Get the minimum location for this pixel.
	 * 
	 * @return
	 * Will not be null.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Set the minimum location for this pixel.
	 * 
	 * @param location
	 * Cannot be null. Does not set priority.
	 */
	public void setLocation( Location location ) {
		Validate.notNull( location );
		this.location = location;
		this.world = location.getWorld();
		this.x = location.getX();
		this.z = location.getZ();
		calculateDimensions();
	}

	/**
	 * Get the color of this pixel.
	 * 
	 * @return
	 * An ARGB color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the color of this pixel.
	 * 
	 * @param color
	 * An ARGB color, cannot be null.
	 */
	public void setColor( Color color ) {
		Validate.notNull( color );
		this.color = color;
	}

	@Override
	public int compareTo( WorldPixel other ) {
		if ( priority > other.priority ) {
			return 1;
		} else {
			return -1;
		}
	}
}
