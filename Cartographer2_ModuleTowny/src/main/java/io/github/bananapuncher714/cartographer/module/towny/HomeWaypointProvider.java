package io.github.bananapuncher714.cartographer.module.towny;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class HomeWaypointProvider implements WorldCursorProvider {
	private Type type;

	public HomeWaypointProvider( Type type ) {
		this.type = type;
	}

	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( viewer.getSetting( TownyModule.TOWNY_SPAWN ) ) {
			Location location = setting.getLocation();
			Resident resident;
			try {
				resident = TownyUniverse.getInstance().getResident( player.getName() );
				if ( resident.hasTown() ) {
					Town town = resident.getTown();

					if ( town.hasSpawn() ) {
						Location home = town.getSpawn().clone();
						if ( home.getWorld() == location.getWorld() ) {
							home.setYaw( setting.isRotating() ? location.getYaw() : 180 );

							return Collections.singleton( new WorldCursor( null, home, type, true ) );
						}
					}
				}
			} catch ( TownyException e ) {
				e.printStackTrace();
			}
		}
		return Collections.emptyList();
	}
}
