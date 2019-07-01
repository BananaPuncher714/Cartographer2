package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;

public class MapData {
	Set< RealWorldCursor > cursors = new HashSet< RealWorldCursor >();

	public Set< RealWorldCursor > getCursors() {
		return cursors;
	}
}
