package io.github.bananapuncher714.cartographer.core;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {
	protected Cartographer plugin;
	protected Set< Location > updateSet;
	
	protected PlayerListener( Cartographer plugin ) {
	}
	
	protected void update() {
	}
}
