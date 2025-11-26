package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * Provides a {@link Minimap} with {@link WorldCursor} that will appear on the canvas, may be called extremely frequently(Several times per tick).
 * 
 * @author BananaPuncher714
 */
public interface WorldCursorProvider extends MapProvider {
	/**
	 * Get a collection of {@link WorldCursor} that will be placed on the map. To place cursors relative to the map canvas, use a {@link MapCursorProvider}.
	 * 
	 * @param player
	 * The player for who this is rendering. Do not use the location of this player, may not be accurate.
	 * @param map
	 * The {@link Minimap} which is requesting the cursors.
	 * @param setting
	 * The {@link PlayerSetting} of the player, including the location of the player that will be used to render the canvas.
	 * @return
	 * A collection of {@link WorldCursor}. Can return null.
	 */
	Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting );
}
