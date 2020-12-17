package io.github.bananapuncher714.cartographer.module.worldguard.implementation.v7;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import io.github.bananapuncher714.cartographer.module.worldguard.api.CuboidRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardWrapper;

public class WorldGuardWrapperImpl implements WorldGuardWrapper {
	@Override
	public Collection< CuboidRegion > getRegionsFor( World world ) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager manager = container.get( BukkitAdapter.adapt( world ) );
		
		Set< CuboidRegion > regions = new HashSet< CuboidRegion >();
		for ( Entry< String, ProtectedRegion > entry : manager.getRegions().entrySet() ) {
			String name = entry.getKey();
			ProtectedRegion region = entry.getValue();
			
			// I don't want to mess with polygonal regions, so cuboid for now
			if ( region instanceof ProtectedCuboidRegion ) {
				ProtectedCuboidRegion cuboid = ( ProtectedCuboidRegion ) region;
				
				BlockVector3 minVec = cuboid.getMinimumPoint();
				BlockVector3 maxVec = cuboid.getMaximumPoint();
				
				Location min = new Location( world, minVec.getX(), minVec.getY(), minVec.getZ() );
				Location max = new Location( world, maxVec.getX(), maxVec.getY(), maxVec.getZ() );
				
				CuboidRegion cuboidRegion = new CuboidRegion( name, min, max );
				
				cuboidRegion.getMembers().addAll( region.getMembers().getUniqueIds() );
				cuboidRegion.getOwners().addAll( region.getOwners().getUniqueIds() );
				
				regions.add( cuboidRegion );
			}
		}
		
		return regions;
	}
}
