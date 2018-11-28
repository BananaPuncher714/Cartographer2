package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

/**
 * Different methods to get maps
 * 
 * @author BananaPuncher714
 */
public interface GeneralUtil {
	MapView getMapViewFrom( ItemStack item );
	boolean isWater( Material material );
}
