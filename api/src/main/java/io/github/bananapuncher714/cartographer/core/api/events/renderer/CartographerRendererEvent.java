package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

/**
 * Base event related to {@link CartographerRenderer}.
 * 
 * @author BananaPuncher714
 */
public abstract class CartographerRendererEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected CartographerRenderer renderer;
	
	/**
	 * Construct a CartographerRendererEvent with the provided {@link CartographerRenderer}.
	 * 
	 * @param renderer
	 * Cannot be null.
	 */
	public CartographerRendererEvent( CartographerRenderer renderer ) {
		Validate.notNull( renderer );
		this.renderer = renderer;
	}
	
	/**
	 * Getter for the {@link CartographerRenderer}.
	 * 
	 * @return
	 * Not null renderer.
	 */
	public CartographerRenderer getRenderer() {
		return renderer;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
