/**
 * Handles simple tests like getting the highest distance at a location and getting the depth of water.
 * 
 * @author BananaPuncher714
 */
package io.github.bananapuncher714.cartographer.core.util;

import java.util.Set;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.Block;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public final class BlockUtil {
	
	public static int getWaterDepth( Block block ) {
		return 0;
	}
	
	public static int getWaterDepth( ChunkSnapshot chunk, int x, int y, int z ) {
		return 0;
	}
	
	public static int getHighestYAt( ChunkSnapshot chunk, int x, int y, int z, Set< CrossVersionMaterial > skip ) {
		return 0;
	}
	
	public static int getHighestYAt( Location location, Set< CrossVersionMaterial > skip ) {
		return 0;
	}
	
	public static Block getNextHighestBlockAt( Location location, Set< CrossVersionMaterial > skip, int height ) {
		return null;
	}
	
	public static double distance( int x1, int y1, int x2, int y2 ) {
		return Math.sqrt( Math.pow( Math.abs( x1 - x2 ), 2 ) + Math.pow( Math.abs( y1 - y2 ), 2 ) );
	}
	
	public static boolean needsRender( ChunkLocation location ) {
		return false;
	}
}
