package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

public interface DataCache {
	void setFileQueue( BigChunkQueue queue );
	DataCache setNotifier( ChunkNotifier notifier );
	ChunkNotifier getChunkNotifier();
	void update();
	void setChunkDataProvider( ChunkDataProvider provider );
	ChunkDataProvider getChunkDataProvider();
	void setChunkDataStorage( ChunkDataStorage storage );
	ChunkDataStorage getStorage();
	void registerSnapshot( ChunkLocation location );
	void unregisterSnapshot( ChunkLocation location );
	boolean hasSnapshot( ChunkLocation location );
	ChunkData getDataAt( ChunkLocation location );
	boolean containsDataAt( ChunkLocation location );
	ChunkSnapshot getChunkSnapshotAt( ChunkLocation location );
	void addToChunkLoader( ChunkLocation location );
	void process( ChunkLocation location, boolean force );
	boolean isProcessing( ChunkLocation location );
	boolean withinVisiblePlayerRange( ChunkLocation location );
	void removeScannedLocation( BigChunkLocation location );
	void removeChunkDataAt( ChunkLocation location );
	void updateLocation( Location location, MinimapPalette palette );
	void updateDataAt( ChunkLocation location, ChunkData data, boolean force );
	void updateDataAt( BigChunkLocation location, BigChunk chunk, boolean force );
	void requestLoadFor( ChunkLocation location, boolean force );
	void requestSnapshotFor( ChunkLocation location, boolean force );
	void requestLoadFor( BigChunkLocation location );
	void terminate();
}
