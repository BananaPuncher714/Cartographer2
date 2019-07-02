package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.UUID;

public class MapViewer {
	protected final UUID uuid;
	protected boolean rotateByDefault = true;
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}

	// TODO Make use of this or something
	public boolean isRotateByDefault() {
		return rotateByDefault;
	}

	public void setRotateByDefault( boolean rotateByDefault ) {
		this.rotateByDefault = rotateByDefault;
	}
}