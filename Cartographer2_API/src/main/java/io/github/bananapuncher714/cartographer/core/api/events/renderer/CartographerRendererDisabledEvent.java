package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

/**
 * Called whenever the renderer is attempting to render a disable map, such as when the player is in a blacklisted world.
 * 
 * @author BananaPuncher714
 */
public class CartographerRendererDisabledEvent extends CartographerRendererEvent {
	private static final HandlerList handlers = new HandlerList();
	
	private byte[] data;
	
	public CartographerRendererDisabledEvent( CartographerRenderer renderer, byte[] data ) {
		super( renderer );
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData( byte[] data ) {
		this.data = data;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
