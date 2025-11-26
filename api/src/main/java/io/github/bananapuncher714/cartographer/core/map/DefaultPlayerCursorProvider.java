package io.github.bananapuncher714.cartographer.core.map;

import java.util.Collection;

import org.bukkit.entity.Player;

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
		return null;
	}
}
