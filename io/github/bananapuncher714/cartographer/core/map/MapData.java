package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;

public class MapData {
	Set< WorldCursor > cursors = new HashSet< WorldCursor >();
	Set< MapPixel > pixels = new HashSet< MapPixel >();
	
	public Set< WorldCursor > getCursors() {
		return cursors;
	}
}
