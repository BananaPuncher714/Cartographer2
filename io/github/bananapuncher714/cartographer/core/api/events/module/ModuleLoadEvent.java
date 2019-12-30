package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.module.Module;

public class ModuleLoadEvent extends ModuleEvent {
	private static final HandlerList handlers = new HandlerList();

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
