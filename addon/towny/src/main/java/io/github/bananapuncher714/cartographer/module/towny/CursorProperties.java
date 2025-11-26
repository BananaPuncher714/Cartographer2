package io.github.bananapuncher714.cartographer.module.towny;

import org.bukkit.map.MapCursor.Type;

public class CursorProperties {
	private boolean enabled;
	private Type type;
	private double globalRange;
	private double townRange;
	private double maxZoom;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType( Type type ) {
		this.type = type;
	}
	
	public double getGlobalRange() {
		return globalRange;
	}
	
	public void setGlobalRange( double globalRange ) {
		this.globalRange = globalRange;
	}
	
	public double getTownRange() {
		return townRange;
	}
	
	public void setTownRange( double townRange ) {
		this.townRange = townRange;
	}

	public double getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom( double maxZoom ) {
		this.maxZoom = maxZoom;
	}
}
