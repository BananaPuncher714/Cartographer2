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
import me.glaremasters.guilds.guild.GuildHome;

public class GuildHomeCursorProvider implements WorldCursorProvider {
	protected GuildsModule module;
	
	public GuildHomeCursorProvider( GuildsModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		List< WorldCursor > cursors = new ArrayList< WorldCursor >();
		CursorProperties properties = module.getHomeProperties();
		if ( properties == null ) {
			return cursors;
		}
		
		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		if ( viewer.getSetting( GuildsModule.GUILDS_SHOW_HOME ) && properties.isEnabled() ) {
			GuildsAPI api = Guilds.getApi();
			Guild guild = api.getGuild( player );
			
			if ( guild != null ) {
				GuildHome home = guild.getHome();
				if ( home != null ) {
					Location location = home.getAsLocation();
					if ( location != null ) {
						Location loc = location.clone();
						Location playerLoc = setting.getLocation();
						loc.setYaw( setting.isRotating() ? playerLoc.getYaw() : 180 );
						
						if ( playerLoc.getWorld() == loc.getWorld() ) {
							double radSquared = properties.getRadius() * properties.getRadius();
						
							if ( radSquared == 0 || loc.distanceSquared( playerLoc ) <= radSquared ) {
								cursors.add( new WorldCursor( properties.getName(), loc, properties.getType(), properties.getVisibility() == CursorVisibility.FULL ) );
							}
						}
					}
				}
			}
		}
		return cursors;
	}

}
