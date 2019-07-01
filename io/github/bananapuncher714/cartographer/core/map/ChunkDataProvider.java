package io.github.bananapuncher714.cartographer.core.map;

import org.bukkit.ChunkSnapshot;

import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

public interface ChunkDataProvider {
	ChunkData process( ChunkSnapshot snapshot );
}
