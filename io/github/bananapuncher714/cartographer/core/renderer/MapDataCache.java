package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChunkSnapshot;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

/**
 * A thread safe cache with chunk data.
 * 
 * Created on 20181128
 * 
 * @author BananaPuncher714
 */
public class MapDataCache {
	protected final Map< ChunkLocation, ChunkData > data;
	protected final Map< ChunkLocation, ChunkSnapshot > chunks;
	
	protected ChunkDataProvider provider;
	
	public MapDataCache( ChunkDataProvider provider ) {
		this.provider = provider;
		data = new ConcurrentHashMap< ChunkLocation, ChunkData >();
		chunks = new ConcurrentHashMap< ChunkLocation, ChunkSnapshot >();
	}
	
	public Map< ChunkLocation, ChunkData > getData() {
		return data;
	}
	
	public ChunkData getDataAt( ChunkLocation location ) {
		return data.get( location );
	}
	
	public boolean contains( ChunkLocation location ) {
		return data.containsKey( location );
	}
	
	public void process( ChunkSnapshot chunk ) {
		new Thread() {
			@Override
			public void run() {
				ChunkData chunkData = provider.process( chunk );
				if ( chunkData != null ) {
					data.put( new ChunkLocation( chunk ), chunkData );
				}
			}
		}.start();
	}
}
