package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldPixel {
	protected World world;
	protected int x, y, z;
	protected Location location;
	protected Color color;
	
	public WorldPixel( Location location, Color color ) {
		this.location = location.clone();
		this.world = location.getWorld();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		this.color = color;
	}
	
	public WorldPixel( World world, int x, int y, int z, Color color ) {
		this.location = new Location( world, x, y, z );
		this.world = world;
		this.x = x;
		this.y = y;
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

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
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
}
