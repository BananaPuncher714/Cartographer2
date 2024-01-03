package io.github.bananapuncher714.cartographer.module.towny;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
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
	private boolean name = false;
	private IconSupplier supplier;
	
	public PlayerMarkerProvider( boolean showName, IconSupplier supplier ) {
		name = showName;
		this.supplier = supplier;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();
		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( viewer.getSetting( TownyModule.TOWNY_PLAYERS ) ) {
			Location location = setting.getLocation();
			for ( Player target : location.getWorld().getPlayers() ) {
//				if ( player != target && !( target.isSneaking() || target.hasPotionEffect( PotionEffectType.INVISIBILITY ) ) ) {
					Location targetLoc = target.getLocation();
					Optional< Type > type = supplier.getIconFor( player, target, setting.getScale() );
					if ( type.isPresent() ) {
						WorldCursor cursor = new WorldCursor( name ? target.getName() : null, targetLoc, type.get(), false );
						cursors.add( cursor );
					}
//				}
			}
		}
		
		return cursors;
	}
	
	public interface IconSupplier {
		Optional< Type > getIconFor( Player viewer, Player target, double zoom );
	}

}
