package io.github.bananapuncher714.cartographer.module.lands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.lands.visibility.CursorVisibility;
import io.github.bananapuncher714.cartographer.module.lands.visibility.LandVisibility;
import me.angeschossen.lands.Lands;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.framework.roles.settings.RoleSetting;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.TrustedPlayer;

public class HomeCursorProvider implements WorldCursorProvider {
	protected LandsModule module;
	
	public HomeCursorProvider( LandsModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		List< WorldCursor > cursors = new ArrayList< WorldCursor >();
		CursorProperties properties = module.getLandSpawnProperties();
		if ( properties == null ) {
			return cursors;
		}
		
		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		LandVisibility landVis = viewer.getSetting( LandsModule.LANDS_SHOW_LANDS );
		LandVisibility spawnVis = viewer.getSetting( LandsModule.LANDS_SHOW_SPAWN );
		if ( landVis != LandVisibility.NONE && spawnVis != LandVisibility.NONE ) {
			UUID uuid = setting.getUUID();
			Collection< Land > lands = module.getIntegration().getLands();
			for ( Land land : lands ) {
				UUID owner = land.getOwnerUID();
				// Check if the player owns the land or can see lands that they're trusted in
				if ( uuid.equals( owner ) || ( landVis != LandVisibility.OWN && spawnVis == LandVisibility.TRUSTED ) ) {
					// Check if the player is the owner or a trusted player or if they have the spawn teleport role
					TrustedPlayer trusted = land.getTrustedPlayer( uuid );
					if ( uuid.equals( owner ) || trusted.isTrustedWholeLand() || land.getDefaultArea().hasFlag( uuid, Flags.SPAWN_TELEPORT ) ) {
						Location loc = land.getSpawn();
						if ( loc != null ) {
							loc = loc.clone();
							Location playerLoc = setting.getLocation();
							loc.setYaw( setting.isRotating() ? playerLoc.getYaw() : 180 );
							
							if ( playerLoc.getWorld() == loc.getWorld() ) {
								double radSquared = properties.getRadius() * properties.getRadius();
							
								if ( radSquared == 0 || loc.distanceSquared( playerLoc ) <= radSquared ) {
									String name = properties.getName().replace( "%%__name__%%", land.getName() );
									name = module.getCartographer().getDependencyManager().translateString( player, name );
									cursors.add( new WorldCursor( name, loc, properties.getType(), properties.getVisibility() == CursorVisibility.FULL ) );
								}
							}
						}
					}
				}
			}
		}
		return cursors;
	}

}
