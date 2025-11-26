package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.module.Module;

/**
 * Called after a {@link Module} gets fully loaded, normally called once.
 * 
 * @author BananaPuncher714
 */
public class ModuleLoadEvent extends ModuleEvent {
	private static final HandlerList handlers = new HandlerList();

	/**
	 * The {@link Module} that has been loaded.
	 * 
	 * @param module
	 * Cannot be null.
	 */
	public ModuleLoadEvent( Module module ) {
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
