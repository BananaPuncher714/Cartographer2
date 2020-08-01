package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

/**
 * Called whenever a player attempts to interact with a {@link CartographerRenderer} when there is a menu active.
 * 
 * @author BananaPuncher714
 */
public class CartographerRendererInteractEvent extends CartographerRendererEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	protected boolean cancelled = false;
	
	protected Player player;
	protected MapMenu menu;
	protected MapInteraction interaction;
	
	public CartographerRendererInteractEvent( Player player, CartographerRenderer renderer, MapMenu menu, MapInteraction interaction ) {
		super( renderer );
		Validate.notNull( player );
		Validate.notNull( menu );
		Validate.notNull( interaction );
		this.player = player;
	}
	
	/**
	 * Getter for the player viewing.
	 * 
	 * @return
	 * The player viewing the map.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Getter for the menu.
	 * 
	 * @return
	 * Not null.
	 */
	public MapMenu getMenu() {
		return menu;
	}

	/**
	 * The interaction the player attempted to perform.
	 * 
	 * @return
	 * Not null.
	 */
	public MapInteraction getInteraction() {
		return interaction;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled( boolean cancelled ) {
		this.cancelled = cancelled;
	}
}
