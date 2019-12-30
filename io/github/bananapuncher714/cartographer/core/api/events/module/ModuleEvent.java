package io.github.bananapuncher714.cartographer.core.api.events.module;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.module.Module;

public abstract class ModuleEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected Module module;
	
	public ModuleEvent( Module module ) {
		this.module = module;
	}
	
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
