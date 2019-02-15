package io.github.bananapuncher714.cartographer.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class PlayerListener implements Listener {
	@EventHandler
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		
		ItemStack item = event.getItem();
		if ( item != null && item.getType() == Material.FILLED_MAP ) {
			MapMeta meta = ( MapMeta ) item.getItemMeta();
			int id = meta.getMapId();
			MapView view = Bukkit.getMap( ( short ) id );
			for ( MapRenderer renderer : view.getRenderers() ) {
				if ( renderer instanceof CartographerRenderer  ) {
					CartographerRenderer cr = ( CartographerRenderer ) renderer;
					
					int scale = ( int ) ( cr.getScale() * 4 );
					
					boolean zoom = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
					// newScale represents how many blocks per pixel, with 16 being the most and .25 being the least
					double newScale = Math.min( 64, Math.max( 1, zoom ? scale << 1 : scale >> 1 ) ) / 4.0;
					cr.setScale( newScale );
				}
			}
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	private void onBlockBreakEvent( BlockBreakEvent event ) {
		Bukkit.getScheduler().runTask( Cartographer.getInstance(), new Runnable() {
			@Override
			public void run() {
				updateMap( event.getBlock().getLocation() );
			}
		} );
	}
	
	@EventHandler
	private void onBlockPlaceEvent( BlockPlaceEvent event ) {
		Bukkit.getScheduler().runTask( Cartographer.getInstance(), new Runnable() {
			@Override
			public void run() {
				updateMap( event.getBlock().getLocation() );
			}
		} );
	}
	
	private void updateMap( Location... locations ) {
		Minimap map = Cartographer.getInstance().getMinimap();
		
		for ( Location location : locations ) {
			map.getDataCache().updateLocation( location, map.getPalette() );
		}
	}
}
