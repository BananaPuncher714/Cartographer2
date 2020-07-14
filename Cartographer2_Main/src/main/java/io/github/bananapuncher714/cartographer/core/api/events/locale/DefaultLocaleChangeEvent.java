package io.github.bananapuncher714.cartographer.core.api.events.locale;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;

public class DefaultLocaleChangeEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected String oldLocale;
	protected String newLocale;
	
	public DefaultLocaleChangeEvent( String old, String newLocale ) {
		this.oldLocale = old;
		this.newLocale = newLocale;
	}
	
	public String getNewLocale() {
		return newLocale;
	}

	public void setNewLocale( String newLocale ) {
		this.newLocale = newLocale;
	}

	public String getOldLocale() {
		return oldLocale;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
