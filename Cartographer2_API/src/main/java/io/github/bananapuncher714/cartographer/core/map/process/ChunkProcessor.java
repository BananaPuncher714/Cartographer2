package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.concurrent.Callable;

import org.bukkit.ChunkSnapshot;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class ChunkProcessor implements Callable< ChunkData > {
	private final ChunkSnapshot snapshot;
	private ChunkDataProvider provider;
	
	ChunkProcessor( ChunkSnapshot snapshot, ChunkDataProvider provider ) {
		this.snapshot = snapshot;
		this.provider = provider;
	}
	
	public ChunkSnapshot getSnapshot() {
		return snapshot;
	}
	
	public ChunkLocation getChunkLocation() {
		return new ChunkLocation( snapshot );
	}
	
	public ChunkDataProvider getDataProvider() {
		return provider;
	}
	
	public void setDataProvider( ChunkDataProvider provider ) {
		this.provider = provider;
	}
	
	@Override
	public ChunkData call() throws Exception {
		return provider.process( snapshot );
	}
}