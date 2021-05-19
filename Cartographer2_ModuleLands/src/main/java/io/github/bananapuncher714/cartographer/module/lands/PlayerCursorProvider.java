package io.github.bananapuncher714.cartographer.module.lands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.lands.visibility.CursorVisibility;
import io.github.bananapuncher714.cartographer.module.lands.visibility.LandVisibility;
import me.angeschossen.lands.api.land.Land;

public class PlayerCursorProvider implements WorldCursorProvider {
	protected LandsModule module;
	
	public PlayerCursorProvider( LandsModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		List< WorldCursor > cursors = new ArrayList< WorldCursor >();
		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		LandVisibility playerVis = viewer.getSetting( LandsModule.LANDS_SHOW_PLAYERS );
		if ( playerVis != LandVisibility.NONE ) {
			LandVisibility landVis = viewer.getSetting( LandsModule.LANDS_SHOW_LANDS );
		
			UUID uuid = setting.getUUID();
			Collection< Land > lands = module.getIntegration().getLands();
			List< Land > ownedLands = lands.stream().filter( land -> { return uuid.equals( land.getOwnerUID() ); } ).collect( Collectors.toList() );
			Location location = setting.getLocation();
			Land occupying = module.getIntegration().getLand( location );
			boolean isVisible = occupying != null &&
					( ( landVis == LandVisibility.TRUSTED && occupying.getTrustedPlayer( uuid ).isTrustedWholeLand() ) ||
							landVis == LandVisibility.ALL );
			for ( Player online : location.getWorld().getPlayers() ) {
				if ( player != online ) {
					CursorProperties properties = module.getLandUntrustedProperties();
					boolean found = false;
					
					// Check if the current land is visible
					if ( isVisible ) {
						// Check if the player is the owner
						if ( online.getUniqueId().equals( occupying.getOwnerUID() ) ) {
							properties = module.getLandOwnerProperties();
							found = true;
						} else if ( playerVis == LandVisibility.ALL &&
								occupying.getTrustedPlayer( online.getUniqueId() ).isTrustedWholeLand() &&
								occupying.getTrustedPlayer( uuid ).isTrustedWholeLand() ) {
							// Both users are trusted members
							properties = module.getLandMemberProperties();
						}
					}
					
					// Check if the player is a trusted player on one of the owner's lands
					if ( !found ) {
						for ( Land owned : ownedLands ) {
							if ( owned.getTrustedPlayer( online.getUniqueId() ).isTrustedWholeLand() ) {
								properties = module.getLandTrustedProperties();
								found = true;
								break;
							}
						}
					}
					
					// Skip if there's no property
					if ( properties == null ) {
						continue;
					}
					
					Location otherLoc = online.getLocation();
					double radSquared = properties.getRadius() * properties.getRadius();
					if ( otherLoc.getWorld() == location.getWorld() && ( radSquared == 0 || otherLoc.distanceSquared( location ) <= radSquared ) ) {
						cursors.add( new WorldCursor( properties.isShowName() ? online.getDisplayName() : null, otherLoc, properties.getType(), properties.getVisibility() == CursorVisibility.FULL ) );
					}
				}
			}
		}
		
		return cursors;
	}

}
