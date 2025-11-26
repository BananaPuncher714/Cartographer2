package io.github.bananapuncher714.cartographer.module.factionsuuid;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

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
		if ( viewer.getSetting( FactionsUUIDModule.FACTION_HOME ) ) {
			Location location = setting.getLocation();
			FPlayer fplayer = FPlayers.getInstance().getByPlayer( player );
			if ( fplayer.hasFaction() ) {
				Faction faction = fplayer.getFaction();
				
				if ( faction.hasHome() ) {
					Location home = faction.getHome().clone();
					if ( home.getWorld() == location.getWorld() ) {
						home.setYaw( setting.isRotating() ? location.getYaw() : 180 );
						
						return Collections.singleton( new WorldCursor( null, home, type, true ) );
					}
				}
			}
		}
		return Collections.emptyList();
	}
}
