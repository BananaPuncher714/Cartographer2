package io.github.bananapuncher714.cartographer.core.api.map;

import java.util.Collection;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;

public interface MapPixelProvider extends Comparable< MapPixelProvider > {
	Collection< MapPixel > getMapPixels( Player player, Minimap map, PlayerSetting setting );
	default PixelPriority getPriority() {
		return PixelPriority.NORMAL;
	}
	
	@Override
	default int compareTo( MapPixelProvider provider ) {
		if ( getPriority() == provider.getPriority() ) {
			if ( provider == this ) {
				return 0;
			} else {
				return 1;
			}
		} else if ( getPriority().isHigherThan( provider.getPriority() ) ) {
			return 1;
		} else {
			return -1;
		}
	}
}
