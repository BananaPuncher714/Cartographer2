package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldPixel implements Comparable< WorldPixel  > {
	protected World world;
	protected double x, depth, z;
	private double centerX, centerY, radX, radY;
	private double width = 1, height = 1;
	protected Location location;
	protected Color color;
	
	public WorldPixel( Location location, Color color ) {
		this.location = location.clone();
		this.world = location.getWorld();
		this.x = location.getBlockX();
		this.depth = location.getBlockY();
		this.z = location.getBlockZ();
		this.color = color;
		calculateDimensions();
	}
	
	public WorldPixel( World world, double x, double z, Color color ) {
		this.location = new Location( world, x, depth, z );
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

	public boolean intersects( double x, double z ) {
		return Math.abs( x - centerX ) <= radX && Math.abs( z - centerY ) <= radY;
	}
	
	public World getWorld() {
		return world;
	}

	public void setWorld( World world ) {
		this.world = world;
	}

	public double getX() {
		return x;
	}

	public void setX( double x ) {
		this.x = x;
		calculateDimensions();
	}

	public double getWidth() {
		return width;
	}

	public void setWidth( double width ) {
		this.width = width;
		calculateDimensions();
	}

	public double getHeight() {
		return height;
	}

	public void setHeight( double height) {
		this.height = height;
		calculateDimensions();
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth( double y ) {
		this.depth = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ( double z ) {
		this.z = z;
		calculateDimensions();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation( Location location ) {
		this.location = location;
	}

	public Color getColor() {
		return color;
	}

	public void setColor( Color color ) {
		this.color = color;
	}

	@Override
	public int compareTo( WorldPixel other ) {
		if ( depth > other.depth ) {
			return 1;
		} else {
			return -1;
		}
	}
}
