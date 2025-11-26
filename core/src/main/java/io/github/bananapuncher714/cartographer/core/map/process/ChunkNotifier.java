package io.github.bananapuncher714.cartographer.core.map.process;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public interface ChunkNotifier {
	ChunkData onChunkLoad( ChunkLocation location, ChunkData data );
	ChunkData onChunkProcessed( ChunkLocation location, ChunkData data );
}