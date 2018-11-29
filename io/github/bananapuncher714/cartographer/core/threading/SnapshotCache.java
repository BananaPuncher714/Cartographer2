package io.github.bananapuncher714.cartographer.core.threading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChunkSnapshot;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public enum SnapshotCache {
	INSTANCE;
	
	private Map< String, Integer > buildHeight = new ConcurrentHashMap< String, Integer >();
	private Map< ChunkLocation, ChunkSnapshot > chunks = new ConcurrentHashMap< ChunkLocation, ChunkSnapshot >();
	
	public ChunkSnapshot getSnapshotAt( ChunkLocation location ) {
		return chunks.get( location );
	}
	
	public int getBuildHeight( String worldName ) {
		return buildHeight.get( worldName );
	}
}
