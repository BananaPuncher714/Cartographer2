package io.github.bananapuncher714.cartographer.core.map;

import org.bukkit.ChunkSnapshot;

public interface ChunkDataProvider {
	ChunkData process( ChunkSnapshot snapshot );
}
