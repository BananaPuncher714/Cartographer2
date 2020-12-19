package io.github.bananapuncher714.cartographer.module.worldguard.api;

import org.bukkit.Location;

public class CuboidRegion extends WorldGuardRegion {
	protected Location max;
	protected Location min;
	
	public CuboidRegion( String name, Location min, Location max ) {
		super( name );
		this.max = max.clone();
		this.min = min.clone();
	}

	public Location getMax() {
		return max.clone();
	}

	public Location getMin() {
		return min.clone();
	}
}
