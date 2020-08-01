package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
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
	
	protected final Set< ChunkLocation > forcedLoading = null;
	protected final Set< ChunkLocation > loaded = null;
	
	protected ChunkDataProvider provider;
	
	protected ChunkNotifier notifier;
	
	protected MapSettings setting;
	
	public MapDataCache( ChunkDataProvider provider, MapSettings setting ) {
	}
	
	public MapDataCache( MapSettings setting ) {
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
	
	public void loadData( ChunkLocation location, ChunkData data ) {
	}
	
	public ChunkSnapshot getChunkSnapshotAt( ChunkLocation location ) {
		return null;
	}
	
	public void releaseSnapshot( ChunkLocation location ) {
	}
	
	/**
	 * Used by other processes to force load a chunk
	 * 
	 * @param location
	 * A location that requires loading
	 */
	public void addToProcessQueue( ChunkLocation location ) {
	}
	
	public void process( ChunkLocation location ) {
	}
	
	public boolean isProcessing( ChunkLocation location ) {
		return false;
	}
	
	/**
	 * Check if it is stored anywhere
	 * 
	 * @param location
	 * @return
	 */
	public boolean absent( ChunkLocation location ) {
		return false;
	}
	
	public boolean withinVisiblePlayerRange( ChunkLocation location ) {
		return false;
	}
	
	public static ChunkState getStateOf( Minimap map, ChunkLocation location ) {
		return null;
	}
	
	public void updateLocation( Location location, MinimapPalette palette ) {
	}
	
	public void terminate() {
	}
	
	public static enum ChunkState {
		NONE,
		QUEUED,
		LOADING,
		WAITING_FOR_PROCESSING,
		PROCESSING,
		FILE_LOADING,
		FILE_SAVING,
		CACHED;
	}
}
