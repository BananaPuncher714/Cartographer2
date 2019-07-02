package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

public interface LocalCursorProvider {
	Collection< MapCursor > getCursors( Player player, Minimap map );
}
