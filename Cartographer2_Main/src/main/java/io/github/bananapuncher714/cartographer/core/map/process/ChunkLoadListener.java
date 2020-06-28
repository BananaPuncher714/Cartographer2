package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * A listener to load chunks and register/unregister chunk snapshots for minimaps.
 * 
 * @author BananaPuncher714
 */
public enum ChunkLoadListener implements Listener {
	INSTANCE;
	
	private final Queue< ChunkLocation > loading = new ArrayDeque< ChunkLocation >();
	private final Set< ChunkLocation > checkSet = new HashSet< ChunkLocation >();
	private final Set< ChunkLocation > beingLoaded = new HashSet< ChunkLocation >();
	
	private boolean isForceLoad = false;
	private int cacheAmount = 50;
	private int loadAmount = 10;
	private int generateAmount = 1;
	
	@EventHandler
	private void onChunkLoadEvent( ChunkLoadEvent event ) {
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			// Add it back to the queue for when ther server isn't overloaded to be processed
			queueChunk( location );
		} else {
			for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
				minimap.getDataCache().registerSnapshot( location );
			}
		}
		// The chunk has been loaded, even if we can't process it immediately
		beingLoaded.remove( location );
	}
	
	@EventHandler
	private void onChunkUnloadEvent( ChunkUnloadEvent event  ) {
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
			minimap.getDataCache().unregisterSnapshot( location );
		}
		beingLoaded.remove( location );
	}
	
	/**
	 * Update every so often to load new chunks.
	 */
	public void update() {
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			return;
		}
		
		double percentage = 0;
		while ( percentage < 1 && !loading.isEmpty() ) {
			ChunkLocation location = loading.peek();
			
			if ( location.isLoaded() ) {
				for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
					minimap.getDataCache().registerSnapshot( location );
				}
				percentage += 1.0 / cacheAmount;
			} else if ( isForceLoad ) {
				percentage += 1.0 / ( location.exists() ? loadAmount : generateAmount );
				// Add it to the list of locations being loaded
				beingLoaded.add( location );
				location.load();
			}
			
			// Remove the current chunk from the queue
			// Either it's loaded and sent off for processing
			// or it's being forcefully loaded
			// or nothing has happened, but an attempt was made
			loading.remove();
			checkSet.remove( location );
		}
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
		return INSTANCE.checkSet.contains( location );
	}
	
	/**
	 * Add the given location to the load queue.
	 * 
	 * @param location
	 * A {@link ChunkLocation} that needs loading.
	 */
	public static void queueChunk( ChunkLocation location ) {
		// Only queue the chunk if it isn't already queued or being loaded
		if ( INSTANCE.checkSet.contains( location ) || INSTANCE.beingLoaded.contains( location ) ) {
			return;
		}
		INSTANCE.loading.add( location );
		INSTANCE.checkSet.add( location );
	}
	
	public static boolean isLoading( ChunkLocation location ) {
		return INSTANCE.beingLoaded.contains( location );
	}
	
	/**
	 * Get the locations that are queued for loading.
	 * 
	 * @return
	 * The current set of chunks.
	 */
	public Set< ChunkLocation > getChunks() {
		return checkSet;
	}

	public boolean isForceLoad() {
		return isForceLoad;
	}

	public void setForceLoad( boolean isForceLoad ) {
		this.isForceLoad = isForceLoad;
	}

	public int getCacheAmount() {
		return cacheAmount;
	}

	public void setCacheAmount( int cacheAmount ) {
		this.cacheAmount = cacheAmount;
	}

	public int getLoadAmount() {
		return loadAmount;
	}

	public void setLoadAmount( int loadAmount ) {
		this.loadAmount = loadAmount;
	}

	public int getGenerateAmount() {
		return generateAmount;
	}

	public void setGenerateAmount( int generateAmount ) {
		this.generateAmount = generateAmount;
	}
}
