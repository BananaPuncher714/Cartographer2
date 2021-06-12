package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.Bukkit;
import org.bukkit.World;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class EnormousChunkLocation {
	private int x, z;
	private String worldName;
	private World world;
	
	public EnormousChunkLocation( World world, int x, int z ) {
		this( world.getName(), x, z );
		this.world = world;
	}
	
	public EnormousChunkLocation( String worldname, int x, int z ) {
		this.worldName = worldname;
		this.x = x;
		this.z = z;
	}
	
	public EnormousChunkLocation( EnormousChunkLocation location ) {
		this( location.getWorldName(), location.getX(), location.getZ() );
	}
	
	public EnormousChunkLocation( ChunkLocation location ) {
		this( location.getWorld(), location.getX() >> 8, location.getZ() >> 8 );
	}
	
	public int getX() {
		return x;
	}
	
	public EnormousChunkLocation setX( int x ) {
		this.x = x;
		return this;
	}
	
	public int getZ() {
		return z;
	}
	
	public EnormousChunkLocation setZ( int z ) {
		this.z = z;
		return this;
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public void setWorldName( String worldName ) {
		this.worldName = worldName;
	}

	public World getWorld() {
		updateWorld();
		return world;
	}

	public void setWorld( World world ) {
		this.world = world;
		this.worldName = world.getName();
	}
	
	private void updateWorld() {
		if ( world == null || !world.getName().equalsIgnoreCase( worldName ) ) {
			world = Bukkit.getWorld( worldName );
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnormousChunkLocation other = (EnormousChunkLocation) obj;
		if (worldName == null) {
			if (other.worldName != null)
				return false;
		} else if (!worldName.equals(other.worldName))
			return false;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
}
