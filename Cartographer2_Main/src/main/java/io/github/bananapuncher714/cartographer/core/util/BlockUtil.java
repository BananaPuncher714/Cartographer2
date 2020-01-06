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

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public final class BlockUtil {
	
	public static int getWaterDepth( Block block ) {
		int depth = 0;
		while ( Cartographer.getUtil().isWater( block ) ) {
			depth++;
			block = block.getRelative( BlockFace.DOWN );
			if ( block == null || block.getY() < 0 ) {
				return depth;
			}
		}
		return depth;
	}
	
	public static int getWaterDepth( ChunkSnapshot chunk, int x, int y, int z ) {
		// 1-2
		// 3-4
		// 5-6
		// 7-9
		// 10+
		int originalY = y - 1;
		while ( y > 0 && Cartographer.getUtil().isWater( chunk, x, y--, z ) );
		return originalY - y;
	}
	
	public static int getHighestYAt( ChunkSnapshot chunk, int x, int y, int z, Set< CrossVersionMaterial > skip ) {
//		while ( y > 0 && ( ( skip != null && skip.contains( Cartographer.getUtil().getBlockType( chunk, x, y--, z ) ) ) || ( skip == null && Cartographer.getUtil().getBlockType( chunk, x, y--, z ).material == Material.AIR ) ) );
		for ( ; y > 0; y-- ) {
			if ( Cartographer.getUtil().isWater( chunk, x, y, z ) ) {
				return y;
			}
			
			CrossVersionMaterial mat = Cartographer.getUtil().getBlockType( chunk, x, y, z );
			if ( skip == null ) {
				if ( mat.material != Material.AIR ) {
					return y;
				}
			} else if ( !skip.contains( mat ) ) {
				return y;
			}
		}
		return y;
	}
	
	public static int getHighestYAt( Location location, Set< CrossVersionMaterial > skip ) {
		int y = location.getWorld().getMaxHeight();
		location.setY( y );
		while ( y > 0 ) {
			if ( skip == null ) {
				if ( location.getBlock().getType() != Material.AIR ) {
					return y;
				}
			} else {
				CrossVersionMaterial blockType = Cartographer.getUtil().getBlockType( location.getBlock() );
				if ( !skip.contains( blockType ) || Cartographer.getUtil().isWater( location.getBlock() ) ) {
					return y;
				}
			}
			location.setY( --y );
		}
		return 0;
	}
	
	public static Block getNextHighestBlockAt( Location location, Set< CrossVersionMaterial > skip, int height ) {
		skip.add( new CrossVersionMaterial( Material.AIR ) );
		Location loc = location.clone();
		if ( height > 0 && height <= loc.getWorld().getMaxHeight() ) {
			loc.setY( height );
		} else {
			loc.setY( 1 );
		}
		Block b = loc.getBlock();
		Block upper = b.getRelative( BlockFace.UP );
		CrossVersionMaterial upperType = Cartographer.getUtil().getBlockType( upper );
		while ( skip.contains( upperType ) && upper.getLocation().getY() < loc.getWorld().getMaxHeight() ) {
			upper = upper.getRelative( BlockFace.UP );
			upperType = Cartographer.getUtil().getBlockType( upper );
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
