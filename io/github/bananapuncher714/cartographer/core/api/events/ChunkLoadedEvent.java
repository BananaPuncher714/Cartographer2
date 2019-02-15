package io.github.bananapuncher714.cartographer.core.api.events;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;

public class ChunkLoadedEvent extends ChunkProcessedEvent {
	public ChunkLoadedEvent( ChunkLocation location, ChunkData chunk ) {
		super( location, chunk );
	}
}
