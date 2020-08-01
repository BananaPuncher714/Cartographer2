package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * Provides a {@link Minimap} with what {@link MapPixel} will appear on the canvas, may be called extremely frequently(Several times per tick).
 * 
 * @author BananaPuncher714
 */
public interface MapPixelProvider extends MapProvider {
	/**
	 * Get a collection of the {@link MapPixel} to draw based on the {@link Minimap} and {@link PlayerSetting} provided.
	 * To place pixels relative to the Minecraft world, use a {@link WorldPixelProvider}.
	 * 
	 * @param player
	 * The player for who this is rendering. Do not use the location of this player, may not be accurate.
	 * @param map
	 * The {@link Minimap} which is requesting {@link MapPixel}.
	 * @param setting
	 * The {@link PlayerSetting} of the player, including the location of the player that will be used to render the canvas.
	 * @return
	 * A collection of {@link MapPixel}. Can return null.
	 */
	Collection< MapPixel > getMapPixels( Player player, Minimap map, PlayerSetting setting );
}
