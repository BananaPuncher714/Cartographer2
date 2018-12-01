package io.github.bananapuncher714.cartographer.core.map;

import io.github.bananapuncher714.cartographer.core.renderer.MapDataCache;

public class Minimap {
	protected MinimapPalette palette;
	protected MapDataCache cache;
	
	public Minimap( String id, MinimapPalette palette, MapDataCache cache ) {
		this.palette = palette;
		this.cache = cache;
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}
	
	public MapDataCache getDataCache() {
		return cache;
	}
}
