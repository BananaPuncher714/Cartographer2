package io.github.bananapuncher714.cartographer.core.map.process;

import java.awt.Color;

import org.apache.commons.lang.Validate;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

/**
 * Simulates vanilla map rendering, except not the water.
 * 
 * @author BananaPuncher714
 */
public class SimpleChunkProcessor implements ChunkDataProvider {
	protected MapDataCache cache;
	protected MinimapPalette palette;
	
	/**
	 * Construct a SimpleChunkProcessor with a cache and palette.
	 * 
	 * @param cache
	 * Cache containing other ChunkSnapshots that can be used. Cannot be null.
	 * @param palette
	 * A palette to get the colors for the blocks. Cannot be null.
	 */
	public SimpleChunkProcessor( MapDataCache cache, MinimapPalette palette ) {
		Validate.notNull( cache );
		Validate.notNull( palette );
		this.cache = cache;
		this.palette = palette;
	}
	
	@Override
	public ChunkData process( ChunkSnapshot snapshot ) {
		int[] buffer = new int[ 16 ];
		ChunkLocation north = new ChunkLocation( snapshot ).subtract( 0, 1 );
		int maxHeight = north.getWorld().getMaxHeight() - 1;
		int minHeight = Cartographer.getUtil().getMinWorldHeight( north.getWorld() );
		ChunkSnapshot northSnapshot = cache.getChunkSnapshotAt( north );
		// Check if the north snapshot exists for the northern border
		if ( northSnapshot == null ) {
			return null;
		}
		for ( int i = 0; i < 16; i++ ) {
			buffer[ i ] = BlockUtil.getHighestYAt( northSnapshot, i, maxHeight, 15, palette.getTransparentBlocks(), minHeight );
		}
		
		byte[] data = new byte[ 256 ];
		
		for ( int z = 0; z < 16; z++ ) {
			for ( int x = 0; x < 16; x++ ) {
				int height = BlockUtil.getHighestYAt( snapshot, x, maxHeight, z, palette.getTransparentBlocks(), minHeight );
				int prevVal = buffer[ x ];
				buffer[ x ] = height;
				Color color = palette.getDefaultColor();
				if ( Cartographer.getUtil().isWater( snapshot, x, height, z ) ) {
					// WATER RENDERING TIME
					int depth = BlockUtil.getWaterDepth( snapshot, x, height, z, minHeight );
					boolean even = ( ( x + z ) & 1 ) == 0;
					// 1-2
					// 3-4
					// 5-6
					// 7-9
					// 10+
					color = palette.getColor( new CrossVersionMaterial( Material.WATER ) );
					if ( depth < 3 ) {
						// Do nothing
					} else if ( depth < 5 ) {
						if ( even ) {
							// Do nothing
						} else {
							color = JetpImageUtil.brightenColor( color, -10 );
						}
					} else if ( depth < 7 ) {
						color = JetpImageUtil.brightenColor( color, -10 );
					} else if ( depth < 10 ) {
						if ( even ) {
							color = JetpImageUtil.brightenColor( color, -10 );
						} else {
							color = JetpImageUtil.brightenColor( color, -30 );
						}
					} else {
						color = JetpImageUtil.brightenColor( color, -30 );
					}
				} else {
					// It's something on land
					CrossVersionMaterial material = Cartographer.getUtil().getBlockType( snapshot, x, height, z );
					color = palette.getColor( material );
					if ( prevVal > 0 ) {
						if ( prevVal == height ) {
							color = JetpImageUtil.brightenColor( color, -10 );
						} else if ( prevVal > height ) {
							color = JetpImageUtil.brightenColor( color, -30 );
						}
					}
					
				}
				data[ x + ( z << 4 ) ] = JetpImageUtil.getBestColorIncludingTransparent( color.getRGB() );
			}
		}
		
		return new ChunkData( data );
	}
	
	@Override
	public int process( Location location, MinimapPalette palette ) {
		Validate.notNull( palette );
		int height = BlockUtil.getHighestYAt( location, palette.getTransparentBlocks() );
		int prevVal = BlockUtil.getHighestYAt( location.clone().subtract( 0, 0, 1 ), palette.getTransparentBlocks() );
		Location highest = location.clone();
		highest.setY( height );
		Block block = highest.getBlock();
		Color color = palette.getDefaultColor();
		if ( Cartographer.getUtil().isWater( block ) ) {
			// WATER RENDERING TIME
			int depth = BlockUtil.getWaterDepth( block );
			int x = block.getX();
			int z = block.getZ();
			boolean even = ( ( x + z ) % 2 ) == 0;
			// 1-2
			// 3-4
			// 5-6
			// 7-9
			// 10+
			color = palette.getColor( new CrossVersionMaterial( Material.WATER ) );
			if ( depth < 3 ) {
				// Do nothing
			} else if ( depth < 5 ) {
				if ( even ) {
					// Do nothing
				} else {
					color = JetpImageUtil.brightenColor( color, -10 );
				}
			} else if ( depth < 7 ) {
				color = JetpImageUtil.brightenColor( color, -10 );
			} else if ( depth < 10 ) {
				if ( even ) {
					color = JetpImageUtil.brightenColor( color, -10 );
				} else {
					color = JetpImageUtil.brightenColor( color, -30 );
				}
			} else {
				color = JetpImageUtil.brightenColor( color, -30 );
			}
		} else {
			// It's something on land
			CrossVersionMaterial material = Cartographer.getInstance().getHandler().getUtil().getBlockType( block );
			color = palette.getColor( material );
			if ( prevVal > 0 ) {
				if ( prevVal == height ) {
					color = JetpImageUtil.brightenColor( color, -10 );
				} else if ( prevVal > height ) {
					color = JetpImageUtil.brightenColor( color, -30 );
				}
			}
		}
		
		return color.getRGB();
	}
}
