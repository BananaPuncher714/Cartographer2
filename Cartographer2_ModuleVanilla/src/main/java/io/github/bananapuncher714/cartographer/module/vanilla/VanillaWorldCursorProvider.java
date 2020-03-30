package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderDeathLocation;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderEntity;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderPlayer;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderSpawnLocation;

public class VanillaWorldCursorProvider implements WorldCursorProvider {
	protected VanillaPlus module;

	private CursorProviderPlayer playerProvider;
	private Map< EntityType, CursorProviderEntity > entityProviders = new HashMap< EntityType, CursorProviderEntity >();
	private CursorProviderDeathLocation deathLoc;
	private CursorProviderSpawnLocation spawnLoc;


	protected VanillaWorldCursorProvider( VanillaPlus module ) {
		this.module = module;

		deathLoc = new CursorProviderDeathLocation( module );
		spawnLoc = new CursorProviderSpawnLocation();
	}

	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();

		if ( module.isWhitelisted( setting.getLocation().getWorld() ) ) {
			PlayerViewer viewer = module.getViewerFor( player.getUniqueId() );

			// Add their last death location
			if ( player.hasPermission( "vanillaplus.location.death" ) ) {
				Set< NamedLocation > deathLocs = deathLoc.getFor( player, setting );
				for ( NamedLocation loc : deathLocs ) {
					WorldCursor cursor = viewer.convert( loc, player, setting );
					if ( cursor != null ) {
						cursors.add( cursor );
					}
				}
			}

			// Add their spawn location
			if ( player.hasPermission( "vanillaplus.location.spawn" ) ) {
				Set< NamedLocation > spawnLocs = spawnLoc.getFor( player, setting );
				for ( NamedLocation loc : spawnLocs ) {
					WorldCursor cursor = viewer.convert( loc, player, setting );
					if ( cursor != null ) {
						cursors.add( cursor );
					}
				}
			}

			// Add other players
			if ( player.hasPermission( "vanillaplus.cursor.players" ) ) {
				for ( Player tracking: playerProvider.getFor( player, setting ) ) {
					WorldCursor cursor = viewer.convert( tracking, player, setting );
					if ( cursor != null ) {
						cursors.add( cursor );
					}
				}
			}

			// Add all the entities
			for ( CursorProviderEntity entityProvider : entityProviders.values() ) {
				if ( player.hasPermission( "vanillaplus.cursor.entity." + entityProvider.getType().name().toLowerCase() ) ) {
					for ( Entity entity : entityProvider.getFor( player, setting ) ) {
						WorldCursor cursor = viewer.convert( entity, player, setting );
						if ( cursor != null ) {
							cursors.add( cursor );
						}
					}
				}
			}
		}

		return cursors;
	}

	protected void setPlayerProvider( CursorProviderPlayer provider ) {
		this.playerProvider = provider;
	}

	protected void addEntityProvider( CursorProviderEntity provider ) {
		entityProviders.put( provider.getType(), provider );
	}
}
