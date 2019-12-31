package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.module.Module;

/**
 * Called after a {@link Module} finishes enabling.
 * 
 * @author BananaPuncher714
 */
public class ModuleEnableEvent extends ModuleEvent {
	private static final HandlerList handlers = new HandlerList();

	/**
	 * The {@link Module} after it has been enabled.
	 * 
	 * @param module
	 * Cannot be null.
	 */
	public ModuleEnableEvent( Module module ) {
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
