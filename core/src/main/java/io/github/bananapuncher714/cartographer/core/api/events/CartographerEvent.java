package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.Event;

import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

/**
 * Parent event for all Cartographer2 events.
 * 
 * @author BananaPuncher714
 */
public abstract class CartographerEvent extends Event {
	/**
	 * Call the event on the main thread.
	 */
	public void callEvent() {
		BukkitUtil.callEventSync( this );
	}
}
