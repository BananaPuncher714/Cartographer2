package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.NamedLocation;

public class CursorProviderSpawnLocation implements ObjectProvider< NamedLocation > {

	@Override
	public Set< NamedLocation > getFor( Player player, PlayerSetting settings ) {
		Set< NamedLocation > locations = new HashSet< NamedLocation >();
		
		Location spawnLoc = player.getBedSpawnLocation();
		if ( spawnLoc == null || spawnLoc.getWorld() != settings.getLocation().getWorld() ) {
			spawnLoc = settings.getLocation().getWorld().getSpawnLocation();
		}
		locations.add( new NamedLocation( "spawn", spawnLoc.clone() ) );
		
		return locations;
	}
}
