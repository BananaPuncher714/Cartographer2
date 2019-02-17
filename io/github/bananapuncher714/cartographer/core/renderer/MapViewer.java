package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.UUID;

public class MapViewer {
	protected final UUID uuid;
	protected boolean rotateByDefault = true;
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}

	public boolean isRotateByDefault() {
		return rotateByDefault;
	}

	public void setRotateByDefault( boolean rotateByDefault ) {
		this.rotateByDefault = rotateByDefault;
	}
}