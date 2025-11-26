package io.github.bananapuncher714.cartographer.module.vanilla;

import org.bukkit.Location;

public class NamedLocation {
	public final String name;
	public final Location location;
	
	public NamedLocation( String name, Location location ) {
		this.name = name;
		this.location = location.clone();
	}
}
