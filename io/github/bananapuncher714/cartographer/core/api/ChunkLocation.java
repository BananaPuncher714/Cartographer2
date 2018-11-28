package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkLocation {

	private int x, z;
	private World world;
	
	public ChunkLocation( World world, int x, int z ) {
		this.x = x;
		this.z = z;
		this.world = world;
	}
	
	public ChunkLocation( Chunk chunk ) {
		world = chunk.getWorld();
		x = chunk.getX();
		z = chunk.getZ();
	}

	public int getX() {
		return x;
	}

	public void setX( int x ) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ( int z ) {
		this.z = z;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld( World world ) {
		this.world = world;
	}
	
	public Chunk getChunk() {
		return world.getChunkAt( x, z );
	}

	@Override
	public int hashCode() {
		final int prime = 797161;
		int result = 1;
		result = prime * result + ( ( world == null ) ? 0 : world.hashCode() );
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		
		ChunkLocation other = ( ChunkLocation ) obj;
		
		if ( world == null ) {
			if ( other.world != null ) {
				return false;
			}
		} else if ( !world.getUID().equals( other.world.getUID() ) ) {
			return false;
		}
		
		if ( x != other.x ) {
			return false;
		}
		if ( z != other.z ) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "ChunkLocation_x=" + x + "_z=" + z + "_world=" + world.getName();
	}
}
