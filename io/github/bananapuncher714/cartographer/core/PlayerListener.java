package io.github.bananapuncher714.cartographer.core;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class PlayerListener implements Listener {
	protected Cartographer plugin;
	protected Set< Location > updateSet = new HashSet< Location >();
	
	protected PlayerListener( Cartographer plugin ) {
		this.plugin = plugin;
		
		Bukkit.getScheduler().runTaskTimer( plugin, this::update, 1, 2 );
	}
	
	private void update() {
		updateMap( updateSet.toArray( new Location[ updateSet.size() ] ) );
		updateSet.clear();
	}
	
	@EventHandler
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( !plugin.getHandler().getUtil().isValidHand( event ) ) {
			return;
		}

		Player player = event.getPlayer();
		
		ItemStack item = event.getItem();
		
		if ( item != null && item.getType() == plugin.getHandler().getUtil().getMapMaterial() ) {
			plugin.getMapManager().update( item );
			MapView view = plugin.getHandler().getUtil().getMapViewFrom( item );
			Minimap currentMap = null;
			ZoomScale newScale = null;
			for ( MapRenderer renderer : view.getRenderers() ) {
				if ( renderer instanceof CartographerRenderer ) {
					CartographerRenderer cr = ( CartographerRenderer ) renderer;
					if ( !cr.isViewing( player.getUniqueId() ) ) {
						continue;
					}
					Minimap map = cr.getMinimap();
					if ( map == null ) {
						continue;
					}
					currentMap = map;
					
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
					newScale = scale;
					
					cr.setScale( player.getUniqueId(), scale.getBlocksPerPixel() );
				}
			}
			
			if ( currentMap != null && plugin.getHandler().mapBug() ) {
				ItemStack newMap = plugin.getMapManager().getItemFor( currentMap );
				MapView newView = plugin.getHandler().getUtil().getMapViewFrom( newMap );
				final ZoomScale finScale = newScale;
				Bukkit.getScheduler().scheduleSyncDelayedTask( plugin, new Runnable() {
					@Override
					public void run() {
						for ( MapRenderer renderer : newView.getRenderers() ) {
							if ( renderer instanceof CartographerRenderer ) {
								CartographerRenderer cr = ( CartographerRenderer ) renderer;
								cr.setScale( player.getUniqueId(), finScale.getBlocksPerPixel() );
							}
						}
					}
				} );
				player.getEquipment().setItemInHand( newMap );
			}
			
			event.setCancelled( true );
		}
	}
	
	@EventHandler
	private void onPlayerLoginEvent( PlayerLoginEvent event ) {
	}
	
	// This is probably the laggiest part of the plugin, mostly since the BlockPhysicsEvent fires a ton
	// The more players, or the more stuff going on the more lag, but the solution is just to get a better computer
	@EventHandler
	private void onBlockPhysicsEvent( BlockPhysicsEvent event ) {
		if ( event.getSourceBlock() == event.getBlock() ) {
			Bukkit.getScheduler().runTask( plugin, new Runnable() {
				@Override
				public void run() {
					addLocation( event.getSourceBlock().getLocation() );
				}
			} );
		}
	}
	
	@EventHandler
	private void onPlayerQuitEvent( PlayerQuitEvent event ) {
		plugin.getProtocol().removeChannel( event.getPlayer() );
	}
	
	private void addLocation( Location location ) {
		updateSet.add( new Location( location.getWorld(), location.getBlockX(), 0, location.getBlockZ() ) );
	}
	
	private void updateMap( Location... locations ) {
		for ( Minimap minimap : plugin.getMapManager().getMinimaps().values() ) {
			for ( Location location : locations ) {
				minimap.getDataCache().updateLocation( location, minimap.getPalette() );
			}
		}
	}
}
