package io.github.bananapuncher714.cartographer.core.map;

import java.awt.Image;
import java.util.UUID;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;

/**
 * Individual per player settings that take priority over Cartographer's default settings but must conform to minimap settings.
 * The player represented may or may not be online.
 * 
 * @author BananaPuncher714
 */
public class MapViewer {
	// This stuff gets saved
	protected UUID uuid;
	protected BooleanOption rotate = BooleanOption.UNSET;
	protected boolean showName = true;
	
	// For on map menu stuff
	protected boolean cursorActive = false;
	
	// This stuff does not get saved
	protected SimpleImage overlay;
	protected SimpleImage background;
	
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public boolean isShowName() {
		return showName;
	}

	public void setShowName( boolean showName ) {
		this.showName = showName;
	}

	public BooleanOption getRotate() {
		return rotate;
	}

	public void setRotate( BooleanOption rotate ) {
		Validate.notNull( rotate );
		this.rotate = rotate;
	}

	public boolean isCursorActive() {
		return cursorActive;
	}

	public void setCursorActive( boolean cursorActive ) {
		this.cursorActive = cursorActive;
	}

	public SimpleImage getOverlay() {
		return overlay;
	}

	public void setOverlay( SimpleImage overlay ) {
		if ( overlay != null ) {
			overlay = new SimpleImage( overlay, 128, 128, Image.SCALE_REPLICATE );
		}
		this.overlay = overlay;
	}

	public SimpleImage getBackground() {
		return background;
	}

	public void setBackground( SimpleImage background ) {
		if ( background != null ) {
			background = new SimpleImage( background, 128, 128, Image.SCALE_REPLICATE );
		}
		this.background = background;
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
