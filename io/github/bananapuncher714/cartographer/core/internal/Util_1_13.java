package io.github.bananapuncher714.cartographer.core.internal;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

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
	public ItemStack getMapItem( int id ) {
		ItemStack map = new ItemStack( Material.FILLED_MAP );
		MapMeta meta = ( MapMeta ) map.getItemMeta();
		meta.setMapId( id );
		map.setItemMeta( meta );
		return map;
	}
	
	@Override
	public boolean isWater( Material material ) {
		return material == Material.WATER;
	}
	
	@Override
	public Material getMapMaterial() {
		return Material.FILLED_MAP;
	}
	
	@Override
	public boolean isValidHand( PlayerInteractEvent event ) {
		return event.getHand() == EquipmentSlot.HAND;
	}
	
	@Override
	public CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z ) {
		return new CrossVersionMaterial( snapshot.getBlockType( x, y, z ) );
	}
	
	@Override
	public CrossVersionMaterial getItemType( ItemStack item ) {
		return new CrossVersionMaterial( item.getType() );
	}
	
	@Override
	public CrossVersionMaterial getBlockType( Block block ) {
		return new CrossVersionMaterial( block.getType() );
	}
}
