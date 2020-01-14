package io.github.bananapuncher714.cartographer.core.map.process;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

public class ChunkSnapshotLite {
	private final CrossVersionMaterial[] blocks = new CrossVersionMaterial[ 65536 ];
	private final long[] water = new long[ 1024 ];
	
	public void set( CrossVersionMaterial type, int x, int y, int z ) {
		
	}

	public CrossVersionMaterial get( int x, int y, int z ) {
		return null;
	}
	
	public void setWater( boolean water, int x, int y, int z ) {
	}
	
	public boolean isWater( int x, int y, int z ) {
		return false;
	}
}
