package io.github.bananapuncher714.cartographer.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class CartographerListener implements Listener {
	@EventHandler
	private void onServerLoadEvent( ServerLoadEvent event ) {
		Cartographer.getInstance().onServerLoad();
	}
}
