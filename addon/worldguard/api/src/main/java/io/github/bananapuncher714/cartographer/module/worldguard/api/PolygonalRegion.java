package io.github.bananapuncher714.cartographer.module.worldguard.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class PolygonalRegion extends WorldGuardRegion {
	private List< Location > locations = new ArrayList< Location >();
	
	public PolygonalRegion( String name ) {
		super( name );
	}

	public List< Location > getLocations() {
		return locations;
	}
}
