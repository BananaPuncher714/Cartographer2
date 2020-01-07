package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class VanillaPlus extends Module {
	private Map< UUID, Location > deaths = new HashMap< UUID, Location >();
	
	@Override
	public void onEnable() {
		registerListener( new VanillaListener( this ) );
		
		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			minimap.registerProvider( new VanillaWorldCursorProvider( this ) );
		}
	}
	
	@Override
	public void onDisable() {
	}
	
	public Location getDeathOf( UUID uuid ) {
		return deaths.get( uuid );
	}
	
	public void setDeathOf( UUID uuid, Location loc ) {
		if ( loc == null ) {
			deaths.remove( uuid );
		} else {
			deaths.put( uuid, loc.clone() );
		}
	}
}
