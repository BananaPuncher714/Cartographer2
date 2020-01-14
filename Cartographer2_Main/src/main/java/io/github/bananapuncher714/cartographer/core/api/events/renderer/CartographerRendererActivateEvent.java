package io.github.bananapuncher714.cartographer.core.api.events.renderer;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

/**
 * Called whenever a map is in a player's hand and gets activated.
 * 
 * @author BananaPuncher714
 */
public class CartographerRendererActivateEvent extends CartographerRendererEvent {
	private static final HandlerList handlers = new HandlerList();
	protected Player player;
	protected boolean mainHand;
	
	public CartographerRendererActivateEvent( Player player, CartographerRenderer renderer, boolean mainHand ) {
		super( renderer );
		Validate.notNull( player );
		this.player = player;
		this.mainHand = mainHand;
	}
	
	/**
	 * Getter for the player viewing
	 * 
	 * @return
	 * The player viewing the map.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Getter for main hand usage.
	 * 
	 * @return
	 * If the map is in the player's main hand.
	 */
	public boolean isMainHand() {
		return mainHand;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
