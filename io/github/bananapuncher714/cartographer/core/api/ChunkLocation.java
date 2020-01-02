package io.github.bananapuncher714.cartographer.core.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a Bukkit chunk's location.
 * 
 * @author BananaPuncher714
 */
public class ChunkLocation {
	private int x, z;
	private String worldName;
	private World world;
	
	/**
	 * Construct a ChunkLocation from a Location.
	 * 
	 * @param location
	 * Cannot be null.
	 */
	public ChunkLocation( Location location ) {
		Validate.notNull( location );
		world = location.getWorld();
		worldName = world.getName();
		x = location.getBlockX() >> 4;
		z = location.getBlockZ() >> 4;
	}
	
	/**
	 * Copy a ChunkLocation.
	 * 
	 * @param location
	 * Cannot be null.
	 */
	public ChunkLocation( ChunkLocation location ) {
		Validate.notNull( location );
		x = location.x;
		z = location.z;
		worldName = location.worldName;
		world = location.world;
	}
	
	/**
	 * Create a ChunkLocation from chunk coordinates.
	 * 
	 * @param world
	 * Cannot be null.
	 * @param x
	 * Chunk coordinate x value.
	 * @param z
	 * Chunk coordinate z value.
	 */
	public ChunkLocation( World world, int x, int z ) {
		Validate.notNull( world );
		this.x = x;
		this.z = z;
		this.worldName = world.getName();
		this.world = world;
		
	}
	
	/**
	 * Create a ChunkLocation from a Bukkit chunk.
	 * 
	 * @param chunk
	 * Cannot be null.
	 */
	public ChunkLocation( Chunk chunk ) {
		Validate.notNull( chunk );
		world = chunk.getWorld();
		worldName = world.getName();
		x = chunk.getX();
		z = chunk.getZ();
	}
	
	/**
	 * Create a ChunkLocation from a ChunkSnapshot.
	 * 
	 * @param snapshot
	 * Cannot be null. The world will not be cached, only the world name.
	 */
	public ChunkLocation( ChunkSnapshot snapshot ) {
		Validate.notNull( snapshot );
		worldName = snapshot.getWorldName();
		x = snapshot.getX();
		z = snapshot.getZ();
	}

	/**
	 * Get the x coordinate.
	 * 
	 * @return
	 * Chunk coordinate x value.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the x coordinate.
	 * 
	 * @param x
	 * Chunk coordinate x value.
	 */
	public ChunkLocation setX( int x ) {
		this.x = x;
		return this;
	}

	/**
	 * Get the z coordinate.
	 * 
	 * @return
	 * Chunk coordinate z value.
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Set the z coordinate.
	 * 
	 * @param z
	 * Chunk coordinate z value.
	 */
	public ChunkLocation setZ( int z ) {
		this.z = z;
		return this;
	}

	
	/**
	 * Add coordinates from current coordinates.
	 * 
	 * @param x
	 * Chunk x coordinate value.
	 * @param z
	 * Chunk z coordinate value.
	 * @return
	 * This location.
	 */
	public ChunkLocation add( int x, int z ) {
		this.x += x;
		this.z += z;
		return this;
	}
	
	/**
	 * Subtract coordinates from current coordinates.
	 * 
	 * @param x
	 * Chunk x coordinate value.
	 * @param z
	 * Chunk z coordinate value.
	 * @return
	 * This location.
	 */
	public ChunkLocation subtract( int x, int z ) {
		this.x -= x;
		this.z -= z;
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
	
	/**
	 * Get the chunk this location represents.
	 * 
	 * @return
	 * May load the chunk.
	 */
	public Chunk getChunk() {
		return world.getChunkAt( x, z );
	}
	
	/**
	 * Check if the chunk that this location represents is loaded.
	 * 
	 * @return
	 * If the chunk is loaded. Does not load the chunk.
	 */
	public boolean isLoaded() {
		return getWorld().isChunkLoaded( x, z );
	}
	
	/**
	 * Load the chunk that this location represents.
	 */
	public void load() {
		getWorld().loadChunk( x, z );
	}
	
	/**
	 * Check if this chunk exists.
	 * 
	 * @return
	 * Checks if the chunk exists, does not have to be loaded.
	 */
	public boolean exists() {
		return getWorld().loadChunk( x, z, false );
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
		
		ChunkLocation other = ( ChunkLocation ) obj;
		
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
		return "ChunkLocation{x:" + x + ",z:" + z + ",world:" + worldName + "}";
	}
}
