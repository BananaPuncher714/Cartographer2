package io.github.bananapuncher714.cartographer.core.api.events;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

public class ChunkLoadedEvent extends ChunkProcessedEvent {
	public ChunkLoadedEvent( Minimap map, ChunkLocation location, ChunkData chunk ) {
		super( map, location, chunk );
	}
}
