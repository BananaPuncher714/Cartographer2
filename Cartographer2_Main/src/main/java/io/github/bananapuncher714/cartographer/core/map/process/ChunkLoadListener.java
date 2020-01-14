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
	
	@EventHandler
	private void onChunkLoadEvent( ChunkLoadEvent event ) {
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			loadChunk( location );
		} else {
			for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
				minimap.getDataCache().registerSnapshot( location );
			}
		}
	}
	
	@EventHandler
	private void onChunkUnloadEvent( ChunkUnloadEvent event  ) {
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
			minimap.getDataCache().unregisterSnapshot( location );
		}
	}
	
	/**
	 * Update every so often to load new chunks.
	 */
	public void update() {
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			return;
		}
		
		for ( int i = 0; i < 100; i++ ) {
			if ( loading.isEmpty() ) {
				break;
			}
			
			ChunkLocation location = loading.poll();
			checkSet.remove( location );
			
			if ( location.isLoaded() ) {
				for ( Minimap minimap : Cartographer.getInstance().getMapManager().getMinimaps().values() ) {
					minimap.getDataCache().registerSnapshot( location );
				}
				i++;
			} else if ( Cartographer.getInstance().isForceLoad() ) {
				i += location.exists() ? 5 : 10;
				location.load();
			}
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
	public static boolean isLoading( ChunkLocation location ) {
		return INSTANCE.checkSet.contains( location );
	}
	
	/**
	 * Add the given location to the load queue.
	 * 
	 * @param location
	 * A {@link ChunkLocation} that needs loading.
	 */
	public static void loadChunk( ChunkLocation location ) {
		if ( INSTANCE.checkSet.contains( location ) ) {
			return;
		}
		INSTANCE.loading.add( location );
		INSTANCE.checkSet.add( location );
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
}
