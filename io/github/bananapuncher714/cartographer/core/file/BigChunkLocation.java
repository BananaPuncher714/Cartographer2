package io.github.bananapuncher714.cartographer.core.file;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

/**
 * Represents a BigChunk location.
 * 
 * @author BananaPuncher714
 */
public class BigChunkLocation {
	private int x, z;
	private String worldName;
	private World world;
	
	/**
	 * Construct a BigChunkLocation from a Location.
	 * 
	 * @param location
	 * Cannot be null.
	 */
	public BigChunkLocation( Location location ) {
		Validate.notNull( location );
		world = location.getWorld();
		worldName = world.getName();
		x = location.getBlockX() >> 8;
		z = location.getBlockZ() >> 8;
	}
	
	/**
	 * Construct a BigChunkLocation from a {@link ChunkLocation}.
	 * 
	 * @param location
	 * Cannot be null.
	 */
	public BigChunkLocation( ChunkLocation location ) {
		Validate.notNull( location );
		world = location.getWorld();
		worldName = world.getName();
		x = location.getX() >> 4;
		z = location.getZ() >> 4;
	}

	/**
	 * Get the x coordinate.
	 * 
	 * @return
	 * A BigChunk x coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the x coordinate
	 * 
	 * @param x
	 * A BigChunk x coordinate.
	 * @return
	 * Itself.
	 */
	public BigChunkLocation setX( int x ) {
		this.x = x;
		return this;
	}

	/**
	 * Get the z coordinate.
	 * 
	 * @return
	 * A BigChunk z coordinate.
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Set the z coordinate
	 * 
	 * @param z
	 * A BigChunk z coordinate.
	 * @return
	 * Itself.
	 */
	public BigChunkLocation setZ( int z ) {
		this.z = z;
		return this;
	}

	/**
	 * Get the world.
	 * 
	 * @return
	 * A non-null world, will fetch and cache the world if not cached already.
	 */
	public World getWorld() {
		if ( world == null ) {
			world = Bukkit.getWorld( worldName );
		}
		return world;
	}

	/**
	 * Set the world.
	 * 
	 * @param world
	 * Cannot be null.
	 */
	public void setWorld( World world ) {
		Validate.notNull( world );
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
