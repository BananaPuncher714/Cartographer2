package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.module.Module;

/**
 * Called after a {@link Module} gets disabled.
 * 
 * @author BananaPuncher714
 */
public class ModuleDisableEvent extends ModuleEvent {
	private static final HandlerList handlers = new HandlerList();

	/**
	 * The {@link Module} after it is disabled.
	 * 
	 * @param module
	 * Cannot be null.
	 */
	public ModuleDisableEvent( Module module ) {
		super( module );
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
