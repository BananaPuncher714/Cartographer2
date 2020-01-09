package io.github.bananapuncher714.cartographer.core.dependency;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class DependencyVanilla {
	public boolean shouldLocationBeLoaded( Location location ) {
		World world = location.getWorld();
		WorldBorder border = world.getWorldBorder();
		
		Location center = border.getCenter();
		double distance = border.getSize() / 2.0;
		
		double x = Math.abs( center.getX() - location.getX() );
		double z = Math.abs( center.getZ() - location.getZ() );
		
		return x <= distance && z <= distance;
	}
}
