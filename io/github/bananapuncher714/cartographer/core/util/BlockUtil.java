/**
 * Handles simple tests like getting the highest distance at a location and getting the depth of water.
 * 
 * @author BananaPuncher714
 */
package io.github.bananapuncher714.cartographer.core.util;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public final class BlockUtil {
	
	public static int getWaterDepth( ChunkSnapshot chunk, int x, int y, int z ) {
		int originalY = y - 1;
		while ( y > 0 && chunk.getBlockType( x, y--, z ) == Material.WATER );
		return originalY - y;
	}
	
	public static int getHighestYAt( ChunkSnapshot chunk, int x, int y, int z, Set< Material > skip ) {
		while ( y > 0 && ( ( skip != null && skip.contains( chunk.getBlockType( x, y--, z ) ) ) || ( skip == null && chunk.getBlockType( x, y--, z ) == Material.AIR ) ) );
		return y + 1;
	}
	
	public static int getHighestYAt( Location location, Set< Material > skip ) {
		// TODO fix this somehow
		int y = location.getWorld().getMaxHeight();
		location.setY( y );
		while ( y > 0 ) {
			if ( skip == null ) {
				if ( location.getBlock().getType() != Material.AIR ) {
					return y;
				}
			} else {
				if ( !skip.contains( location.getBlock().getType() ) ) {
					return y;
				}
			}
			location.setY( --y );
		}
		return 0;
	}
	
	public static Block getNextHighestBlockAt( Location location, Set< Material > skip, int height ) {
		skip.add( Material.AIR );
		Location loc = location.clone();
		if ( height > 0 && height <= loc.getWorld().getMaxHeight() ) {
			loc.setY( height );
		} else {
			loc.setY( 1 );
		}
		Block b = loc.getBlock();
		Block upper = b.getRelative( BlockFace.UP );
		while ( skip.contains( upper.getType() ) && upper.getLocation().getY() < loc.getWorld().getMaxHeight() ) {
			upper = upper.getRelative( BlockFace.UP );
		}
		return upper;
	}
	
	public static double distance( int x1, int y1, int x2, int y2 ) {
		return Math.sqrt( Math.pow( Math.abs( x1 - x2 ), 2 ) + Math.pow( Math.abs( y1 - y2 ), 2 ) );
	}
	
	public static boolean needsRender( ChunkLocation location ) {
		int cx = location.getX() >> 4 << 8;
		int cz = location.getZ() >> 4 << 8;
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			Location playerLoc = player.getLocation();
			int x = playerLoc.getBlockX();
			int z = playerLoc.getBlockZ();
			// Normally 1500
			if ( BlockUtil.distance( cx, cz, x, z ) < 1700 ) {
				return true;
			}
		}
		return false;
	}
}
