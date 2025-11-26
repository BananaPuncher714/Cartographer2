package io.github.bananapuncher714.cartographer.module.factionsuuid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.potion.PotionEffectType;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class PlayerMarkerProvider implements WorldCursorProvider {
	private int rangeSquared;
	private boolean name = false;
	private IconSupplier supplier;
	
	public PlayerMarkerProvider( int range, boolean showName, IconSupplier supplier ) {
		rangeSquared = range * range;
		name = showName;
		this.supplier = supplier;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();
		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( viewer.getSetting( FactionsUUIDModule.FACTION_PLAYERS ) ) {
			Location location = setting.getLocation();
			for ( Player target : location.getWorld().getPlayers() ) {
				if ( player != target && !( target.isSneaking() || target.hasPotionEffect( PotionEffectType.INVISIBILITY ) ) ) {
					Location targetLoc = target.getLocation();
					if ( targetLoc.distanceSquared( location ) < rangeSquared ) {
						Type type = supplier.getIconFor( player, target );
						if ( type != null ) {
    						WorldCursor cursor = new WorldCursor( name ? target.getName() : null, targetLoc, type, false );
    						cursors.add( cursor );
						}
					}
				}
			}
		}
		
		return cursors;
	}
	
	public interface IconSupplier {
		Type getIconFor( Player viewer, Player target );
	}

}
