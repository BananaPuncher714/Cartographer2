package io.github.bananapuncher714.cartographer.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

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
}
