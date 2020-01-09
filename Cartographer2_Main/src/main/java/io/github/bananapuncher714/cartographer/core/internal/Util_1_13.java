package io.github.bananapuncher714.cartographer.core.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

public class Util_1_13 implements GeneralUtil {
	private static Method GETMAP;

	public Util_1_13( boolean subclass ) {
		if ( !subclass ) {
			try {
				GETMAP = Bukkit.class.getMethod( "getMap", short.class );
			} catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
	}

	@Override
	public MapView getMapViewFrom( ItemStack item ) {
		if ( item == null ) {
			return null;
		}
		ItemMeta meta = item.getItemMeta();
		if ( meta instanceof MapMeta ) {
			// This pains me right here
			// It's right in the middle of 1.13 and 1.12
			return getMap( ( ( MapMeta ) meta ).getMapId() );
		}
		return null;
	}

	@Override
	public MapView getMap( int id ) {
		try {
			return ( MapView ) GETMAP.invoke( null, ( short ) id );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getId( MapView view ) {
		Validate.notNull( view );
		return view.getId();
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
	public boolean isWater( Block block ) {
		BlockData data = block.getBlockData();
		if ( data instanceof Waterlogged ) {
			Waterlogged waterlogged = ( Waterlogged ) data;
			return waterlogged.isWaterlogged();
		}
		return data.getMaterial() == Material.WATER || data.getMaterial() == Material.SEAGRASS || data.getMaterial() == Material.TALL_SEAGRASS || data.getMaterial() == Material.KELP_PLANT || data.getMaterial() == Material.KELP;
	}

	@Override
	public boolean isWater( ChunkSnapshot snapshot, int x, int y, int z ) {
		BlockData data = snapshot.getBlockData( x, y, z );
		if ( data instanceof Waterlogged ) {
			Waterlogged waterlogged = ( Waterlogged ) data;
			return waterlogged.isWaterlogged();
		}
		return data.getMaterial() == Material.WATER || data.getMaterial() == Material.SEAGRASS || data.getMaterial() == Material.TALL_SEAGRASS || data.getMaterial() == Material.KELP_PLANT || data.getMaterial() == Material.KELP;
	}

	@Override
	public Material getMapMaterial() {
		return Material.FILLED_MAP;
	}

	@Override
	public boolean isValidHand( PlayerInteractEvent event ) {
		Validate.notNull( event );
		return event.getHand() == EquipmentSlot.HAND;
	}

	@Override
	public ItemStack getMainHandItem( Player player ) {
		return player.getInventory().getItemInMainHand();
	}

	@Override
	public ItemStack getOffHandItem( Player player ) {
		return player.getInventory().getItemInOffHand();
	}

	@Override
	public CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z ) {
		return new CrossVersionMaterial( snapshot.getBlockType( x, y, z ) );
	}

	@Override
	public CrossVersionMaterial getItemType( ItemStack item ) {
		Validate.notNull( item );
		return new CrossVersionMaterial( item.getType() );
	}

	@Override
	public CrossVersionMaterial getBlockType( Block block ) {
		Validate.notNull( block );
		return new CrossVersionMaterial( block.getType() );
	}

	@Override
	public boolean updateEvent( BlockPhysicsEvent event ) {
		return true;
	}
}
