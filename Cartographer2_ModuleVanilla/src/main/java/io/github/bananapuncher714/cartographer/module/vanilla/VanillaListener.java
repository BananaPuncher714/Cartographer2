package io.github.bananapuncher714.cartographer.module.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;

public class VanillaListener implements Listener {
	protected VanillaPlus plugin;
	
	protected VanillaListener( VanillaPlus plugin ) {
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		event.getMinimap().registerProvider( new VanillaWorldCursorProvider( plugin ) );
	}
	
	@EventHandler
	private void onEvent( PlayerDeathEvent event ) {
		plugin.setDeathOf( event.getEntity().getUniqueId(), event.getEntity().getLocation() );
	}
}
