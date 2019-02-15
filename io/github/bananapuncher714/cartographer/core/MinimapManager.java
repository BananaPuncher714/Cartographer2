package io.github.bananapuncher714.cartographer.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

public class MinimapManager {
	Map< String, Minimap > minimaps = new ConcurrentHashMap< String, Minimap >();
	
	public MinimapManager() {
		
	}
}
