package io.github.bananapuncher714.cartographer.core.internal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;

public class Util_1_13 implements GeneralUtil {

	@Override
	public MapView getMapViewFrom( ItemStack item ) {
		if ( item == null ) {
			return null;
		}
		ItemMeta meta = item.getItemMeta();
		if ( meta instanceof MapMeta ) {
			// This pains me right here
			// It's right in the middle of 1.13 and 1.12
			return Bukkit.getMap( ( short ) ( ( MapMeta ) meta ).getMapId() );
		}
		return null;
	}

	@Override
	public boolean isWater( Material material ) {
		return material == Material.WATER;
	}

}
