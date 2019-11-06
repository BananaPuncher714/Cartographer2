package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;

public interface WorldCursorProvider {
	Collection< RealWorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting );
}
