package io.github.bananapuncher714.cartographer.core.api.events.player;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class MapViewerChangeSettingEvent< T extends Comparable< T > > extends MapViewerEvent {
	private static final HandlerList handlers = new HandlerList();
	
	private SettingState< T > state;
	private T newVal;
	
	public MapViewerChangeSettingEvent( MapViewer viewer, SettingState< T > setting, T newVal ) {
		super( viewer );
		this.state = setting;
		this.newVal = newVal;
	}
	
	public T getNewVal() {
		return newVal;
	}

	public void setNewVal( T newVal ) {
		this.newVal = newVal;
	}

	public SettingState< T > getSetting() {
		return state;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
