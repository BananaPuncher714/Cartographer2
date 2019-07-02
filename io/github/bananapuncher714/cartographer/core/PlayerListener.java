package io.github.bananapuncher714.cartographer.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class PlayerListener implements Listener {
	@EventHandler
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		Player player = event.getPlayer();
		
		ItemStack item = event.getItem();
		
		if ( item != null && item.getType() == Material.FILLED_MAP ) {
			Cartographer.getInstance().getMapManager().update( item );
			MapView view = Cartographer.getInstance().getHandler().getUtil().getMapViewFrom( item );
			for ( MapRenderer renderer : view.getRenderers() ) {
				if ( renderer instanceof CartographerRenderer  ) {
					CartographerRenderer cr = ( CartographerRenderer ) renderer;
					if ( !cr.isViewing( player.getUniqueId() ) ) {
						continue;
					}
					Minimap map = cr.getMinimap();
					if ( map == null ) {
						continue;
					}
					
					ZoomScale scale = cr.getScale( player.getUniqueId() );
					
					boolean zoom = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
					if ( map.getSettings().isCircularZoom() ) {
						scale = zoom ? scale.unzoom( map.getSettings().isCircularZoom() ) : scale.zoom( map.getSettings().isCircularZoom() );
						while ( !map.getSettings().isValidZoom( scale ) ) {
							scale = zoom ? scale.unzoom( map.getSettings().isCircularZoom() ) : scale.zoom( map.getSettings().isCircularZoom() );
						}
					} else {
						if ( zoom ) {
							ZoomScale lastValid = scale;
							scale = scale.zoom( false );
							while ( !map.getSettings().isValidZoom( scale ) && !scale.isMostZoomed() ) {
								scale = scale.zoom( false );
							}
							if ( !map.getSettings().isValidZoom( scale ) ) {
								scale = lastValid;
							}
						} else {
							ZoomScale lastValid = scale;
							scale = scale.unzoom( false );
							while ( !map.getSettings().isValidZoom( scale ) && !scale.isLeastZoomed() ) {
								scale = scale.unzoom( false );
							}
							if ( !map.getSettings().isValidZoom( scale ) ) {
								scale = lastValid;
							}
						}
					}
					
					cr.setScale( player.getUniqueId(), scale.getBlocksPerPixel() );
				}
			}
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	private void onPlayerLoginEvent( PlayerLoginEvent event ) {
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
	
	@EventHandler
	private void onWaterFlowEvent( BlockFromToEvent event ) {
		Bukkit.getScheduler().runTask( Cartographer.getInstance(), new Runnable() {
			@Override
			public void run() {
				updateMap( event.getBlock().getLocation() );
				updateMap( event.getToBlock().getLocation() );
			}
		} );
	}
	
	@EventHandler
	private void onBlockPhysicsEvent( BlockPhysicsEvent event ) {
		Bukkit.getScheduler().runTask( Cartographer.getInstance(), new Runnable() {
			@Override
			public void run() {
				updateMap( event.getBlock().getLocation() );
			}
		} );
	}
	
	@EventHandler
	private void onPlayerQuitEvent( PlayerQuitEvent event ) {
		Cartographer.getInstance().getProtocol().removeChannel( event.getPlayer() );
	}
	
	private void updateMap( Location... locations ) {
		for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
			for ( Location location : locations ) {
				minimap.getDataCache().updateLocation( location, minimap.getPalette() );
			}
		}
		
	}
}
