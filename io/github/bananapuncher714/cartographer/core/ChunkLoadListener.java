package io.github.bananapuncher714.cartographer.core;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.test.IntegratedPanel;

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
		Minimap map = Cartographer.getInstance().getMinimap();
		
		map.getDataCache().registerSnapshot( location );
	}
	
	@EventHandler
	private void onChunkUnloadEvent( ChunkUnloadEvent event  ) {
		ChunkLocation location = new ChunkLocation( event.getChunk() );
		Minimap map = Cartographer.getInstance().getMinimap();
		
		map.getDataCache().unregisterSnapshot( location );
	}
	
	protected void update() {
		for ( int i = 0; i < 100; i++ ) {
			if ( loading.isEmpty() ) {
				break;
			}
			
			ChunkLocation location = loading.poll();
			checkSet.remove( location );
			
			Minimap map = Cartographer.getInstance().getMinimap();
			
			// Either load at 100 loaded chunks per second, 10 generated chunks per tick, or 2 ungenerated per tick.
			if ( location.isLoaded() ) {
				map.getDataCache().registerSnapshot( location );
				i += 1;
			} else {
				i += location.exists() ? 10 : 50;
				location.load();
			}
		}
	}
	
	public static boolean isLoading( ChunkLocation location ) {
		return INSTANCE.checkSet.contains( location );
	}
	
	public static void loadChunk( ChunkLocation location ) {
		if ( INSTANCE.checkSet.contains( location ) ) {
			return;
		}
		INSTANCE.loading.add( location );
		INSTANCE.checkSet.add( location );
	}
	
	public Set< ChunkLocation > getChunks() {
		return checkSet;
	}
}
