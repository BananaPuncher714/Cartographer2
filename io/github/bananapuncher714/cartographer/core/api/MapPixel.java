package io.github.bananapuncher714.cartographer.core.api;

import java.awt.Color;

public class MapPixel {
	protected int x, z;
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
}
