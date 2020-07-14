package io.github.bananapuncher714.cartographer.core;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;

public class MapListener implements Listener {
	private Cartographer plugin;
	
	protected MapListener( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onInventoryClickEvent( InventoryClickEvent event ) {
		if ( event.getWhoClicked().hasPermission( "cartographer.admin" ) ) {
			return;
		}
		
		ItemStack item = event.isShiftClick() ? event.getCurrentItem() : event.getCursor();
		if ( item == null || item.getType() == Material.AIR ) {
			return;
		}
		if ( !plugin.getMapManager().isMinimapItem( item ) ) {
			return;
		}
		
		if ( event.getSlot() != event.getRawSlot() && !event.isShiftClick() ) {
			return;
		}
		
		if ( !plugin.isValidInventory( event.getView().getBottomInventory().getType() ) || !plugin.isValidInventory( event.getView().getTopInventory().getType() ) ) {
			event.setCancelled( true );
		}
		
	}
	
	@EventHandler
	private void onInventoryDragEvent( InventoryDragEvent event ) {
		if ( event.getWhoClicked().hasPermission( "cartographer.admin" ) ) {
			return;
		}
		
		ItemStack item = event.getOldCursor();
		if ( item == null || item.getType() == Material.AIR ) {
			return;
		}
		if ( !plugin.getMapManager().isMinimapItem( item ) ) {
			return;
		}
		if ( plugin.isValidInventory( event.getView().getTopInventory().getType() ) ) {
			return;
		}
		for ( int slot : event.getRawSlots() ) {
			if ( event.getInventorySlots().contains( slot ) ) {
				event.setCancelled( true );
				return;
			}
		}
	}
	
	@EventHandler
	private void onEvent( PrepareItemCraftEvent event ) {
		for ( ItemStack item : event.getInventory().getMatrix() ) {
			if ( item != null && plugin.getMapManager().isMinimapItem( item ) ) {
				event.getInventory().setResult( new ItemStack( Material.AIR ) );
			}
		}
	}
	
	@EventHandler
	private void onEvent( PlayerDropItemEvent event ) {
		if ( plugin.isPreventDrop() && !plugin.isUseDropPacket() ) {
			ItemStack item = event.getItemDrop().getItemStack();
			if ( plugin.getMapManager().isMinimapItem( item ) ) {
				event.setCancelled( true );
				plugin.getMapManager().activate( event.getPlayer(), MapInteraction.Q );
			}
		}
	}
}
