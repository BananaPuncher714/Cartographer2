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
public class MapDataCache implements DataCache {
	protected final ExecutorService service = null;
	protected final Map< ChunkLocation, Future< ChunkData > > renderers = null;
	protected ChunkDataStorage storage;
	protected final Map< ChunkLocation, ChunkSnapshot > chunks = null;

	protected final Set< BigChunkLocation > scanned = null;

	protected ChunkDataProvider provider;
	protected ChunkNotifier notifier;

	// Minimap specific objects
	protected MapSettings setting;
	protected BigChunkQueue queue;

	protected final ReentrantLock lock = null;

	public MapDataCache( ChunkDataProvider provider, MapSettings setting ) {
	}

	public MapDataCache( MapSettings setting ) {
	}

	@Override
	public void setFileQueue( BigChunkQueue queue ) {
	}

	@Override
	public MapDataCache setNotifier( ChunkNotifier notifier ) {
		return null;
	}

	@Override
	public ChunkNotifier getChunkNotifier() {
		return null;
	}

	@Override
	public void update() {
	}

	@Override
	public void setChunkDataProvider( ChunkDataProvider provider ) {
	}

	@Override
	public ChunkDataProvider getChunkDataProvider() {
		return null;
	}

	@Override
	public void setChunkDataStorage( ChunkDataStorage storage ) {
	}
	
	@Override
	public ChunkDataStorage getStorage() {
		return null;
	}
	
	@Override
	public void registerSnapshot( ChunkLocation location ) {
	}

	@Override
	public void unregisterSnapshot( ChunkLocation location ) {
	}

	@Override
	public boolean hasSnapshot( ChunkLocation location ) {
		return false;
	}

	@Override
	public ChunkData getDataAt( ChunkLocation location ) {
		return null;
	}

	@Override
	public boolean containsDataAt( ChunkLocation location ) {
		return false;
	}

	@Override
	public ChunkSnapshot getChunkSnapshotAt( ChunkLocation location ) {
		return null;
	}

	/**
	 * Used by other processes to force load a chunk
	 * 
	 * @param location
	 * A location that requires loading
	 */
	@Override
	public void addToChunkLoader( ChunkLocation location ) {
	}

	@Override
	public void process( ChunkLocation location, boolean force ) {
	}

	@Override
	public boolean isProcessing( ChunkLocation location ) {
		return false;
	}

	@Override
	public boolean withinVisiblePlayerRange( ChunkLocation location ) {
		return false;
	}

	@Override
	public void removeScannedLocation( BigChunkLocation location ) {
	}

	@Override
	public void removeChunkDataAt( ChunkLocation location ) {
	}

	@Override
	public void updateLocation( Location location, MinimapPalette palette ) {
	}

	@Override
	public void updateDataAt( ChunkLocation location, ChunkData data, boolean force ) {
	}

	@Override
	public void updateDataAt( BigChunkLocation location, BigChunk chunk, boolean force ) {
	}

	@Override
	public void requestLoadFor( ChunkLocation location, boolean force ) {
	}

	@Override
	public void requestSnapshotFor( ChunkLocation location, boolean force ) {
	}

	@Override
	public void requestLoadFor( BigChunkLocation location ) {
	}

	@Override
	public void terminate() {
	}
}
