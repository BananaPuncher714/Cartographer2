package io.github.bananapuncher714.cartographer.core.map;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.map.MapCursorProvider;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class DefaultPointerCursorProvider implements MapCursorProvider {
	@Override
	public Collection< MapCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		return null;
	}
}
