package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.UUID;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;

public class PlayerSetting {
	protected long lastUpdated = System.currentTimeMillis();
	protected CartographerRenderer renderer;
	protected UUID playerUUID;
	protected Location location;
	protected double zoomscale = 1;
	protected String map;
	protected boolean rotating = Cartographer.getInstance().isRotateByDefault();
	protected boolean mainhand;
	
	protected double cursorX;
	protected double cursorY;
	protected double cursorCenter;
	
	protected MapInteraction interaction;
	protected MapMenu menu;
	
	protected PlayerSetting( CartographerRenderer renderer, UUID uuid, String map, Location location ) {
		this.renderer = renderer;
		this.playerUUID = uuid;
		this.map = map;
		this.location = location;
	}
	
	public PlayerSetting setScale( double scale ) {
		this.zoomscale = scale;
		return this;
	}
	
	public UUID getUUID() {
		return playerUUID;
	}
	
	public boolean isRotating() {
		return rotating;
	}
	
	public String getMap() {
		return map;
	}
	
	public MapMenu getMenu() {
		return menu;
	}
	
	public double getScale() {
		return zoomscale;
	}
	
	public MapInteraction getInteraction() {
		return interaction;
	}
	
	public boolean isMainHand() {
		return mainhand;
	}
	
	public double getCursorX() {
		return cursorX;
	}

	public void setCursorX( double cursorX ) {
		this.cursorX = cursorX;
	}

	public double getCursorY() {
		return cursorY;
	}

	public void setCursorY( double cursorY ) {
		this.cursorY = cursorY;
	}

	public double getCursorYaw() {
		return cursorCenter;
	}

	public void setCursorYaw( double cursorCenter ) {
		this.cursorCenter = cursorCenter;
	}

	public Location getLocation() {
		return location.clone();
	}
	
	public int[] getMapCoordOf( Location point ) {
		return MapUtil.getLocationToPixel( location, point, zoomscale, Math.toRadians( rotating ? ( location.getYaw() + 180 ) : 0 ) );
	}
	
	protected void deactivate() {
		renderer.setScale( playerUUID, zoomscale );
		if ( menu != null ) {
			menu.onClose( playerUUID );
			menu = null;
		}
	}
}