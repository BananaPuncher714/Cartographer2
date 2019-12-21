package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldPixel implements Comparable< WorldPixel  > {
	protected World world;
	protected int x, depth, z;
	protected Location location;
	protected Color color;
	
	public WorldPixel( Location location, Color color ) {
		this.location = location.clone();
		this.world = location.getWorld();
		this.x = location.getBlockX();
		this.depth = location.getBlockY();
		this.z = location.getBlockZ();
		this.color = color;
	}
	
	public WorldPixel( World world, int x, int z, Color color ) {
		this.location = new Location( world, x, depth, z );
		this.world = world;
		this.x = x;
		this.z = z;
		this.color = color;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth( int y ) {
		this.depth = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
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
