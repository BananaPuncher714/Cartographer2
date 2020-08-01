package io.github.bananapuncher714.cartographer.core.api.events.locale;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;

public class LocaleFinishReloadEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
