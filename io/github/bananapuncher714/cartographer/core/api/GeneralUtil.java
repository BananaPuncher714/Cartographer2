package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

/**
 * Different methods that change across various NMS versions
 * 
 * @author BananaPuncher714
 */
public interface GeneralUtil {
	MapView getMapViewFrom( ItemStack item );
	ItemStack getMapItem( int id );
	boolean isWater( Material material );
}
