package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.api.ZoomScale;

public class MapSettings {
	protected Set< ZoomScale > allowedZooms = new HashSet< ZoomScale >();
	protected ZoomScale defaultZoom = ZoomScale.ONE;
	
	public ZoomScale getDefaultZoom() {
		return defaultZoom;
	}
	
	public void setDefaultZoom( ZoomScale defaultZoom ) {
		this.defaultZoom = defaultZoom;
	}
	
}
