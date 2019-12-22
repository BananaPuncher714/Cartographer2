package io.github.bananapuncher714.cartographer.core;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class CartographerListener implements Listener {
	
	// Detect for server load, the runnable is for versions that don't have ServerLoadEvent
	protected CartographerListener() {
		Bukkit.getScheduler().scheduleSyncDelayedTask( Cartographer.getInstance(), Cartographer.getInstance()::onServerLoad );
	}
	
	@EventHandler
	private void onServerLoadEvent( ServerLoadEvent event ) {
		Cartographer.getInstance().onServerLoad();
	}
}
