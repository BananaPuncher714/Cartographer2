package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.module.Module;

/**
 * Event pertaining to a {@link Module}.
 * 
 * @author BananaPuncher714
 */
public abstract class ModuleEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected Module module;
	
	/**
	 * Construct a new ModuleEvent with the provided {@link Module}.
	 * 
	 * @param module
	 * Cannot be null.
	 */
	public ModuleEvent( Module module ) {
		Validate.notNull( module );
		this.module = module;
	}
	
	/**
	 * The {@link Module} involved in this event.
	 * 
	 * @return
	 * A non-null {@link Module}.
	 */
	public Module getModule() {
		return module;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
