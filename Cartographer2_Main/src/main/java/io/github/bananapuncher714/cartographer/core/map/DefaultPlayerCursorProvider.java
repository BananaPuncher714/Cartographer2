package io.github.bananapuncher714.cartographer.core.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * Draw the player on the map.
 * 
 * @author BananaPuncher714
 */
public class DefaultPlayerCursorProvider implements WorldCursorProvider {
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldCursor > cursors = new HashSet< WorldCursor >();
		
		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );
		
		cursors.add( new WorldCursor( viewer.getSetting( MapViewer.SHOWNAME ) ? player.getName() : null, setting.getLocation(), Type.WHITE_POINTER, true ) );
		return cursors;
	}

}
