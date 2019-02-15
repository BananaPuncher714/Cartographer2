package io.github.bananapuncher714.cartographer.core.file;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class BigChunkLocation {
	private int x, z;
	private String worldName;
	private World world;
	
	public BigChunkLocation( Location location ) {
		world = location.getWorld();
		worldName = world.getName();
		x = location.getBlockX() >> 8;
		z = location.getBlockZ() >> 8;
	}
	
	public BigChunkLocation( ChunkLocation location ) {
		world = location.getWorld();
		worldName = world.getName();
		x = location.getX() >> 4;
		z = location.getZ() >> 4;
	}

	public int getX() {
		return x;
	}

	public BigChunkLocation setX( int x ) {
		this.x = x;
		return this;
	}

	public int getZ() {
		return z;
	}

	public BigChunkLocation setZ( int z ) {
		this.z = z;
		return this;
	}

	public World getWorld() {
		if ( world == null ) {
			world = Bukkit.getWorld( worldName );
		}
		return world;
	}

	public void setWorld( World world ) {
		this.world = world;
		this.worldName = world.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 797161;
		int result = 1;
		result = prime * result + ( ( worldName == null ) ? 0 : worldName.hashCode() );
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
		
		BigChunkLocation other = ( BigChunkLocation ) obj;
		
		if ( worldName == null ) {
			if ( other.worldName != null ) {
				return false;
			}
		} else if ( !worldName.equals( other.worldName ) ) {
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
		return "BigChunkLocation{x:" + x + ",z:" + z + ",world:" + worldName + "}";
	}
}
