package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

public class MapPixel implements Comparable< MapPixel > {
	protected int x, z;
	protected int depth;
	protected Color color;
	
	public MapPixel( int x, int z, Color color ) {
		this.x = x;
		this.z = z;
		this.color = color;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public Color getColor() {
		return color;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth( int depth ) {
		this.depth = depth;
	}

	@Override
	public int compareTo( MapPixel pixel ) {
		if ( depth > pixel.depth ) {
			return 1;
		} else {
			return -1;
		}
	}
}
