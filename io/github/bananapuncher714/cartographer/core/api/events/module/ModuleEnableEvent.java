package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.module.Module;

public class ModuleEnableEvent extends ModuleEvent {
	private static final HandlerList handlers = new HandlerList();

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
