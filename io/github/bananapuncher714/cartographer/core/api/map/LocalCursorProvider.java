package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;

public interface LocalCursorProvider {
	/**
	 * Get a collection of cursors that can be placed on the map
	 * 
	 * @param player
	 * The player
	 * @param map
	 * The minimap which this is for
	 * @param setting
	 * The settings of the player, such as rotation, and the position of the player that is going to be rendered
	 * @return
	 * A collection of the cursors
	 */
	Collection< MapCursor > getCursors( Player player, Minimap map, PlayerSetting setting );
}
