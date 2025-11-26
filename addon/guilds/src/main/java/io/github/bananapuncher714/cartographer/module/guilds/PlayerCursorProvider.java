package io.github.bananapuncher714.cartographer.module.guilds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;

public class PlayerCursorProvider implements WorldCursorProvider {
	protected GuildsModule module;
	
	public PlayerCursorProvider( GuildsModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		List< WorldCursor > cursors = new ArrayList< WorldCursor >();
		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		GuildsAPI api = Guilds.getApi();
		Guild guild = api.getGuild( player );
		Location playerLoc = setting.getLocation();
		
		for ( Player other : playerLoc.getWorld().getPlayers() ) {
			if ( other == player ) {
				continue;
			}
			
			Guild otherGuild = api.getGuild( other );

			CursorProperties properties = module.getNeutralProperties();
			if ( guild == otherGuild && guild != null ) {
				if ( !viewer.getSetting( GuildsModule.GUILDS_SHOW_MEMBERS ) ) {
					continue;
				}
				
				GuildRole role = api.getGuildRole( other );
				CursorProperties potential = module.getRoleProperties( role.getLevel() );
				if ( potential != null ) {
					properties = potential;
				}
			} else if ( guild != null && otherGuild != null && guild.getAllies().contains( otherGuild.getId() ) ) {
				if ( !viewer.getSetting( GuildsModule.GUILDS_SHOW_ALLIES ) ) {
					continue;
				}
				properties = module.getAllyProperties();
			} else if ( !viewer.getSetting( GuildsModule.GUILDS_SHOW_NEUTRAL ) ) {
				continue;
			}
			
			if ( properties == null ) {
				continue;
			}
			
			Location otherLoc = other.getLocation();
			double radSquared = properties.getRadius() * properties.getRadius();
			if ( radSquared == 0 || otherLoc.distanceSquared( playerLoc ) <= radSquared ) {
				cursors.add( new WorldCursor( properties.isShowName() ? other.getDisplayName() : null, otherLoc, properties.getType(), properties.getVisibility() == CursorVisibility.FULL ) );
			}
		}
		return cursors;
	}

}
