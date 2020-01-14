package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class CartographerRendererDeactivateEvent extends CartographerRendererEvent {
	private static final HandlerList handlers = new HandlerList();
	protected UUID player;
	
	/**
	 * 
	 * @param player
	 * @param renderer
	 */
	public CartographerRendererDeactivateEvent( UUID player, CartographerRenderer renderer ) {
		super( renderer );
		Validate.notNull( player );
		this.player = player;
	}
	
	/**
	 * Getter for the uuid.
	 * 
	 * @return
	 * The UUID of the player who stopped viewing the renderer.
	 */
	public UUID getUUID() {
		return player;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
