/**
 * Handles simple tests like getting the highest distance at a location and getting the depth of water.
 * 
 * @author BananaPuncher714
 */
package io.github.bananapuncher714.cartographer.core.util;

import java.util.Set;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class BlockUtil {
	
	public static int getWaterDepth( ChunkSnapshot chunk, int x, int y, int z ) {
		int originalY = y - 1;
		while ( y > 0 && chunk.getBlockType( x, y--, z ) == Material.WATER );
		return originalY - y;
	}
	
	public static int getHighestYAt( ChunkSnapshot chunk, int x, int y, int z, Set< Material > skip ) {
		while ( y > 0 && ( ( skip != null && skip.contains( chunk.getBlockType( x, y--, z ) ) ) || chunk.getBlockType( x, y--, z ) == Material.AIR ) );
		return y + 1;
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
}
