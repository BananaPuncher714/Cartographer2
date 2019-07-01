package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

public interface MapPixelProvider {
	Collection< MapPixel > getMapPixels( Player player, Minimap map );
}
