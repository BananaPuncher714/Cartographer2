package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * Different methods that change across various NMS versions
 * 
 * @author BananaPuncher714
 */
public interface GeneralUtil {
	MapView getMapViewFrom( ItemStack item );
	int getId( MapView view );
	ItemStack getMapItem( int id );
	Material getMapMaterial();
	boolean isValidHand( PlayerInteractEvent event );
	boolean isWater( Material material );
	CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z );
	CrossVersionMaterial getItemType( ItemStack item );
	CrossVersionMaterial getBlockType( Block block );
}
