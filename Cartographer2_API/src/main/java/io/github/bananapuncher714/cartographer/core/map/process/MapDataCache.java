package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

/**
 * A thread safe cache with chunk data.
 * 
 * @author BananaPuncher714
 */
public class MapDataCache {
	protected final ExecutorService service = null;
	protected final Map< ChunkLocation, Future< ChunkData > > renderers = null;
	protected final Map< ChunkLocation, ChunkData > data = null;
	protected final Map< ChunkLocation, ChunkSnapshot > chunks = null;

	protected final Set< BigChunkLocation > scanned = null;

	protected ChunkDataProvider provider;
	protected ChunkNotifier notifier;

	protected MapSettings setting;
	protected BigChunkQueue queue;

	protected final ReentrantLock lock = null;

	public MapDataCache( ChunkDataProvider provider, MapSettings setting ) {
	}

	public MapDataCache( MapSettings setting ) {
	}

	public void setFileQueue( BigChunkQueue queue ) {
	}

	public MapDataCache setNotifier( ChunkNotifier notifier ) {
		return null;
	}

	public ChunkNotifier getChunkNotifier() {
		return null;
	}

	public void update() {
	}

	public void setChunkDataProvider( ChunkDataProvider provider ) {
	}

	public ChunkDataProvider getChunkDataProvider() {
		return null;
	}

	public Map< ChunkLocation, ChunkData > getData() {
		return null;
	}

	public void registerSnapshot( ChunkLocation location ) {
	}

	public void unregisterSnapshot( ChunkLocation location ) {
	}

	public boolean hasSnapshot( ChunkLocation location ) {
		return false;
	}

	public ChunkData getDataAt( ChunkLocation location ) {
		return null;
	}

	public boolean containsDataAt( ChunkLocation location ) {
		return false;
	}

	public ChunkSnapshot getChunkSnapshotAt( ChunkLocation location ) {
		return null;
	}

	/**
	 * Used by other processes to force load a chunk
	 * 
	 * @param location
	 * A location that requires loading
	 */
	public void addToChunkLoader( ChunkLocation location ) {
	}

	public void process( ChunkLocation location, boolean force ) {
	}

	public boolean isProcessing( ChunkLocation location ) {
		return false;
	}

	public boolean withinVisiblePlayerRange( ChunkLocation location ) {
		return false;
	}

	public void removeScannedLocation( BigChunkLocation location ) {
	}

	public void removeChunkDataAt( ChunkLocation location ) {
	}

	public void updateLocation( Location location, MinimapPalette palette ) {
	}

	public void updateDataAt( ChunkLocation location, ChunkData data, boolean force ) {
	}

	public void updateDataAt( BigChunkLocation location, BigChunk chunk, boolean force ) {
	}

	public void requestLoadFor( ChunkLocation location, boolean force ) {
	}

	public void requestSnapshotFor( ChunkLocation location, boolean force ) {
	}

	public void requestLoadFor( BigChunkLocation location ) {
	}

	public void terminate() {
	}
}
