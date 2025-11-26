package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private final ReentrantLock lock = new ReentrantLock();
	
	@EventHandler
	private void onChunkLoadEvent( ChunkLoadEvent event ) {
		// Capture this chunk and pass it off to the minimaps for processing
		ChunkLocation location = new ChunkLocation( event.getChunk() );

		INSTANCE.lock.lock();
		// Remove it from the being loaded queue
		beingLoaded.remove( location );
		
		// First, check if it was loaded forcefully
		if ( isForceLoad ) {
			// Capture the snapshot as soon as possible
			for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
				minimap.getDataCache().registerSnapshot( location );
			}
			
			if ( checkSet.remove( location ) ) {
				loading.remove( location );
			}
		} else {
			// We can load it again later, since it was probably loaded in naturally
			loading.add( location );
			checkSet.add( location );
		}
		INSTANCE.lock.unlock();
	}
	
	@EventHandler
	private void onChunkUnloadEvent( ChunkUnloadEvent event  ) {
		INSTANCE.lock.lock();
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
			minimap.getDataCache().unregisterSnapshot( location );
		}
		// Somehow it's already loaded, but says it's still being loaded?
		beingLoaded.remove( location );
		INSTANCE.lock.unlock();
	}
	
	/**
	 * Update every so often to load new chunks.
	 */
	public void update() {
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			return;
		}
		
		double percentage = 0;
		INSTANCE.lock.lock();
		int looped = 0;
		while ( percentage < 1 && !loading.isEmpty() && looped++ < loading.size() ) {
			ChunkLocation location = loading.peek();
			
			if ( location.isLoaded() ) {
				for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
					minimap.getDataCache().registerSnapshot( location );
				}
				percentage += 1.0 / cacheAmount;
				beingLoaded.remove( location );
				
				// Only remove if successfully loaded
				loading.remove();
				checkSet.remove( location );
			} else if ( isForceLoad ) {
				percentage += 1.0 / ( location.exists() ? loadAmount : generateAmount );
				// Add it to the list of locations being loaded and force load it
				beingLoaded.add( location );
				location.getChunk();
				
				loading.remove();
				checkSet.remove( location );
			} else {
				// Don't keep the loop stalled forever
				percentage += .0004;
				// Rotate chunks to the back of the queue
				loading.add( loading.poll() );
			}
		}
		INSTANCE.lock.unlock();
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
		INSTANCE.lock.lock();
		boolean contains = INSTANCE.checkSet.contains( location );
		INSTANCE.lock.unlock();
		return contains;
	}
	
	/**
	 * Add the given location to the load queue.
	 * 
	 * @param location
	 * A {@link ChunkLocation} that needs loading.
	 */
	public static void queueChunk( ChunkLocation location ) {
		// Only queue the chunk if it isn't already queued or being loaded
		INSTANCE.lock.lock();
		boolean required = !( INSTANCE.checkSet.contains( location ) || INSTANCE.beingLoaded.contains( location ) );
		if ( required ) {
			INSTANCE.loading.add( location );
			INSTANCE.checkSet.add( location );
		}
		INSTANCE.lock.unlock();
	}
	
	public static boolean isLoading( ChunkLocation location ) {
		INSTANCE.lock.lock();
		boolean isLoading = INSTANCE.beingLoaded.contains( location );
		INSTANCE.lock.unlock();
		return isLoading;
	}
	
	/**
	 * Get the locations that are queued for loading.
	 * 
	 * @return
	 * The current set of chunks.
	 */
	public Set< ChunkLocation > getChunks() {
		INSTANCE.lock.lock();
		Set< ChunkLocation > locations = new HashSet< ChunkLocation >( checkSet );
		INSTANCE.lock.unlock();
		return locations;
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
