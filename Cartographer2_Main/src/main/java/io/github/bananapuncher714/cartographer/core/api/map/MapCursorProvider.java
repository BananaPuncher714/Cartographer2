package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * Provides a {@link Minimap} with cursors that will appear on the canvas, may be called extremely frequently(Several times per tick).
 * 
 * @author BananaPuncher714
 */
public interface MapCursorProvider extends MapProvider {
	/**
	 * Get a collection of cursors that can be placed on the map. To place cursors relative to the Minecraft world, use a {@link WorldCursorProvider}.
	 * 
	 * @param player
	 * The player for who this is rendering. Do not use the location of this player, may not be accurate.
	 * @param map
	 * The {@link Minimap} which is requesting the cursors.
	 * @param setting
	 * The {@link PlayerSetting} of the player, including the location of the player that will be used to render the canvas.
	 * @return
	 * A collection of {@link MapPixel}. Can return null.
	 */
	Collection< MapCursor > getCursors( Player player, Minimap map, PlayerSetting setting );
}
