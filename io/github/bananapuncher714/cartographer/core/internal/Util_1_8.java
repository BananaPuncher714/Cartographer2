package io.github.bananapuncher714.cartographer.core.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

public class Util_1_8 implements GeneralUtil {
	private static Method GETBLOCKTYPEID;
	private static Method GETMATERIAL;
	private static Method GETBLOCKDATA;
	
	static {
		try {
			GETBLOCKTYPEID = ChunkSnapshot.class.getMethod( "getBlockTypeId", int.class, int.class, int.class );
			GETBLOCKDATA = ChunkSnapshot.class.getMethod( "getBlockData", int.class, int.class, int.class );
			GETMATERIAL = Material.class.getMethod( "getMaterial", int.class );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public MapView getMapViewFrom( ItemStack item ) {
		if ( item == null ) {
			return null;
		}
		if ( item.getType() == Material.MAP ) {
			return Bukkit.getMap( ( short ) item.getDurability() );
		}
		return null;
	}

	@Override
	public ItemStack getMapItem( int id ) {
		ItemStack map = new ItemStack( Material.MAP );
		map.setDurability( ( short ) id );
		return map;
	}
	
	@Override
	public boolean isWater( Material material ) {
		return material == Material.WATER;
	}
	
	@Override
	public Material getMapMaterial() {
		return Material.MAP;
	}
	
	@Override
	public boolean isValidHand( PlayerInteractEvent event ) {
		return true;
	}
	
	@Override
	public CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z ) {
		try {
			int blockId = ( int ) GETBLOCKTYPEID.invoke( snapshot, x, y, z );
			int data = ( int ) GETBLOCKDATA.invoke( snapshot, x, y, z );
			
			return new CrossVersionMaterial( ( Material ) GETMATERIAL.invoke( null, blockId ), data );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public CrossVersionMaterial getItemType( ItemStack item ) {
		return new CrossVersionMaterial( item.getType(), item.getDurability() );
	}
	
	@Override
	public CrossVersionMaterial getBlockType( Block block ) {
		return new CrossVersionMaterial( block.getType(), ( int ) block.getData() );
	}
}
