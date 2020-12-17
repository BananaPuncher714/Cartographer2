package io.github.bananapuncher714.cartographer.module.worldguard.api;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

public class CuboidRegion {
	protected String name;
	protected Set< UUID > members = new HashSet< UUID >();
	protected Set< UUID > owners = new HashSet< UUID >();
	protected Location max;
	protected Location min;
	
	public CuboidRegion( String name, Location min, Location max ) {
		this.name = name;
		this.max = max.clone();
		this.min = min.clone();
	}

	public Set< UUID > getMembers() {
		return members;
	}

	public Set< UUID > getOwners() {
		return owners;
	}

	public Location getMax() {
		return max.clone();
	}

	public Location getMin() {
		return min.clone();
	}

	public String getName() {
		return name;
	}
}
