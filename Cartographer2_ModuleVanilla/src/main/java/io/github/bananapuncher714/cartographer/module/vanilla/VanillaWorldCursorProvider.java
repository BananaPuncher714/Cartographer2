package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class VanillaWorldCursorProvider implements WorldCursorProvider {
	protected VanillaPlus module;
	
	protected VanillaWorldCursorProvider( VanillaPlus module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();
		
		Location playerLoc = player.getLocation();
		
		Location deathLoc = module.getDeathOf( player.getUniqueId() );
		if ( deathLoc != null ) {
			Location loc = deathLoc.clone();
			loc.setYaw( setting.isRotating() ? playerLoc.getYaw() : 180 );
			
			cursors.add( new WorldCursor( "Death", loc, Type.RED_X, true ) );
		}
		
		Location spawnLoc = player.getBedSpawnLocation();
		if ( spawnLoc == null ) {
			spawnLoc = player.getWorld().getSpawnLocation();
		}
		spawnLoc = spawnLoc.clone();
		spawnLoc.setYaw( setting.isRotating() ? playerLoc.getYaw() : 180 );
		cursors.add( new WorldCursor( "Spawn", spawnLoc, Type.MANSION, true ) );
		
		return cursors;
	}

}
