package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Set;

import org.bukkit.event.Listener;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

/**
 * A listener to load chunks and register/unregister chunk snapshots for minimaps.
 * 
 * @author BananaPuncher714
 */
public enum ChunkLoadListener implements Listener {
	INSTANCE;
	
	/**
	 * Update every so often to load new chunks.
	 */
	public void update() {
	}
	
	/**
	 * Check if the given {@link ChunkLocation} is loading.
	 * 
	 * @param location
	 * {@link ChunkLocation} to check.
	 * @return
	 * Whether or not it is being queued for loading.
	 */
	public static boolean isQueued( ChunkLocation location ) {
		return false;
	}
	
	/**
	 * Add the given location to the load queue.
	 * 
	 * @param location
	 * A {@link ChunkLocation} that needs loading.
	 */
	public static void queueChunk( ChunkLocation location ) {
	}
	
	public static boolean isLoading( ChunkLocation location ) {
		return false;
	}
	
	/**
	 * Get the locations that are queued for loading.
	 * 
	 * @return
	 * The current set of chunks.
	 */
	public Set< ChunkLocation > getChunks() {
		return null;
	}

	public boolean isForceLoad() {
		return false;
	}

	public void setForceLoad( boolean isForceLoad ) {
	}

	public int getCacheAmount() {
		return 0;
	}

	public void setCacheAmount( int cacheAmount ) {
	}

	public int getLoadAmount() {
		return 0;
	}

	public void setLoadAmount( int loadAmount ) {
	}

	public int getGenerateAmount() {
		return 0;
	}

	public void setGenerateAmount( int generateAmount ) {
	}
}
