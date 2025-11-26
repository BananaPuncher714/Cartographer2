package io.github.bananapuncher714.cartographer.module.worldguard.implementation.v6;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import io.github.bananapuncher714.cartographer.module.worldguard.api.CuboidRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.PolygonalRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardWrapper;

public class WorldGuardWrapperImpl implements WorldGuardWrapper {
	@Override
	public Collection< WorldGuardRegion > getRegionsFor( World world ) {
		RegionManager manager = WorldGuardPlugin.inst().getRegionManager( world );
		
		Set< WorldGuardRegion > regions = new HashSet< WorldGuardRegion >();
		for ( Entry< String, ProtectedRegion > entry : manager.getRegions().entrySet() ) {
			String name = entry.getKey();
			ProtectedRegion region = entry.getValue();
			
			// I don't want to mess with polygonal regions, so cuboid for now
			if ( region instanceof ProtectedCuboidRegion ) {
				ProtectedCuboidRegion cuboid = ( ProtectedCuboidRegion ) region;
				
				BlockVector minVec = cuboid.getMinimumPoint();
				BlockVector maxVec = cuboid.getMaximumPoint();
				
				Location min = new Location( world, minVec.getX(), minVec.getY(), minVec.getZ() );
				Location max = new Location( world, maxVec.getX(), maxVec.getY(), maxVec.getZ() );
				
				CuboidRegion cuboidRegion = new CuboidRegion( name, min, max );
				
				cuboidRegion.getMembers().addAll( region.getMembers().getUniqueIds() );
				cuboidRegion.getOwners().addAll( region.getOwners().getUniqueIds() );
				
				regions.add( cuboidRegion );
			} else if ( region instanceof ProtectedPolygonalRegion ) {
				ProtectedPolygonalRegion polygon = ( ProtectedPolygonalRegion ) region;
				
				PolygonalRegion polyRegion = new PolygonalRegion( name );
				for ( BlockVector2D vec : polygon.getPoints() ) {
					Location loc = new Location( world, vec.getX(), polygon.getMinimumPoint().getY(), vec.getZ() );
					polyRegion.getLocations().add( loc );
				}
				
				regions.add( polyRegion );
			}
		}
		
		return regions;
	}
}
