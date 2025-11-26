package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

/**
 * Called whenever a new {@link CartographerRenderer} has been created.
 * 
 * @author BananaPuncher714
 */
public class CartographerRendererCreateEvent extends CartographerRendererEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public CartographerRendererCreateEvent( CartographerRenderer renderer ) {
		super( renderer );
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
