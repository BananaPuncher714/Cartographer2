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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class PlayerListener implements Listener {
	protected Cartographer plugin;
	protected Set< Location > updateSet = new HashSet< Location >();
	
	protected PlayerListener( Cartographer plugin ) {
		this.plugin = plugin;
		
		Bukkit.getScheduler().runTaskTimer( plugin, this::update, 1, 5 );
	}
	
	protected void update() {
		updateMap( updateSet.toArray( new Location[ updateSet.size() ] ) );
		updateSet.clear();
	}
	
	@EventHandler
	private void onPlayerSneakEvent( PlayerToggleSneakEvent event ) {
		MapInteraction interaction = event.isSneaking() ? MapInteraction.SHIFT : MapInteraction.UNSHIFT;
		plugin.getMapManager().activate( event.getPlayer(), interaction );
	}
	
	@EventHandler
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( !plugin.getHandler().getUtil().isValidHand( event ) ) {
			return;
		}
		if ( event.getAction() == Action.PHYSICAL ) {
			return;
		}
		
		Player player = event.getPlayer();
		
		ItemStack item = event.getItem();
		
		if ( item != null && item.getType() == plugin.getHandler().getUtil().getMapMaterial() && plugin.getMapManager().isMinimapItem( item ) ) {
			MapView view = plugin.getHandler().getUtil().getMapViewFrom( item );
			plugin.getMapManager().update( item );
			CartographerRenderer renderer = plugin.getMapManager().getRendererFrom( view );
			
			MapMenu menu = renderer.getMenu( player.getUniqueId() );
			
			if ( menu != null ) {
				renderer.interact( player, event.getAction().name().contains( "LEFT" ) ? MapInteraction.LEFT : MapInteraction.RIGHT );
			} else {
				plugin.getMapManager().update( item );
				CartographerRenderer cr = renderer;
				if ( !cr.isViewing( player.getUniqueId() ) ) {
					return;
				}
				Minimap map = cr.getMinimap();
				if ( map == null ) {
					return;
				}

				double scale = cr.getScale( player.getUniqueId() );

				boolean zoom = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
				scale = zoom ? map.getSettings().getPreviousZoom( scale ) : map.getSettings().getNextZoom( scale );
				
				cr.setScale( player.getUniqueId(), scale );

				if ( plugin.getHandler().mapBug() ) {
					ItemStack newMap = plugin.getMapManager().getItemFor( map );
					MapView newView = plugin.getHandler().getUtil().getMapViewFrom( newMap );
					final double finScale = scale;
					Bukkit.getScheduler().scheduleSyncDelayedTask( plugin, new Runnable() {
						@Override
						public void run() {
							for ( MapRenderer renderer : newView.getRenderers() ) {
								if ( renderer instanceof CartographerRenderer ) {
									CartographerRenderer cr = ( CartographerRenderer ) renderer;
									cr.setScale( player.getUniqueId(), finScale );
								}
							}
						}
					} );
					player.getEquipment().setItemInHand( newMap );
				}
			}
			event.setCancelled( true );
		}
	}
	
	// This is probably the laggiest part of the plugin, mostly since the BlockPhysicsEvent fires a ton
	// The more players, or the more stuff going on the more lag, but the solution is just to get a better computer
	@EventHandler
	private void onBlockPhysicsEvent( BlockPhysicsEvent event ) {
		if ( Cartographer.getInstance().getHandler().getUtil().updateEvent( event ) ) {
			Bukkit.getScheduler().runTask( plugin, new Runnable() {
				@Override
				public void run() {
					addLocation( event.getBlock().getLocation() );
				}
			} );
		}
	}
	
	@EventHandler
	private void onPlayerJoinEvent( PlayerJoinEvent event ) {
		plugin.getPlayerManager().getViewerFor( event.getPlayer().getUniqueId() );
	}

	@EventHandler
	private void onPlayerQuitEvent( PlayerQuitEvent event ) {
		plugin.getProtocol().removeChannel( event.getPlayer() );
		plugin.getPlayerManager().unload( event.getPlayer().getUniqueId() );
	}
	
	private void addLocation( Location location ) {
		updateSet.add( new Location( location.getWorld(), location.getBlockX(), 0, location.getBlockZ() ) );
	}
	
	private void updateMap( Location... locations ) {
		for ( Minimap minimap : plugin.getMapManager().getMinimaps().values() ) {
			for ( Location location : locations ) {
				minimap.updateLocation( location );
			}
		}
	}
}
