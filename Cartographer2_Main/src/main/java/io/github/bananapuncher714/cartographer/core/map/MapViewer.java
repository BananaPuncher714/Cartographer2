package io.github.bananapuncher714.cartographer.core.map;

import java.util.UUID;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;

/**
 * Individual per player settings that take priority over Cartographer's default settings but must conform to minimap settings.
 * The player represented may or may not be online.
 * 
 * @author BananaPuncher714
 */
public class MapViewer {
	protected UUID uuid;
	protected BooleanOption rotate = BooleanOption.UNSET;
	
	// For on map menu stuff
	protected boolean cursorActive = false;
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public BooleanOption getRotate() {
		return rotate;
	}

	public void setRotate( BooleanOption rotate ) {
		this.rotate = rotate;
	}

	public boolean isCursorActive() {
		return cursorActive;
	}

	public void setCursorActive( boolean cursorActive ) {
		this.cursorActive = cursorActive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapViewer other = (MapViewer) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}
