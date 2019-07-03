package io.github.bananapuncher714.cartographer.core.map.process;

import java.awt.Color;

import org.bukkit.ChunkSnapshot;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkDataProvider;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class SimpleChunkProcessor implements ChunkDataProvider {
	protected MapDataCache cache;
	protected MinimapPalette palette;
	
	public SimpleChunkProcessor( MapDataCache cache, MinimapPalette palette ) {
		this.cache = cache;
		this.palette = palette;
	}
	
	@Override
	public ChunkData process( ChunkSnapshot snapshot ) {
		int[] buffer = new int[ 16 ];
		ChunkLocation north = new ChunkLocation( snapshot ).subtract( 0, 1 );
		ChunkSnapshot northSnapshot = cache.getChunkSnapshotAt( north );
		// Check if the north snapshot exists for the northern border
		if ( northSnapshot == null ) {
			return null;
		}
		for ( int i = 0; i < 16; i++ ) {
			buffer[ i ] = BlockUtil.getHighestYAt( northSnapshot, i, 255, 15, palette.getTransparentBlocks() );
		}
		
		byte[] data = new byte[ 256 ];
		
		
		for ( int x = 0; x < 16; x++ ) {
			for ( int z = 0; z < 16; z++ ) {
				int height = BlockUtil.getHighestYAt( snapshot, x, 255, z, palette.getTransparentBlocks() );
				int prevVal = buffer[ x ];
				buffer[ x ] = height;
				CrossVersionMaterial material = Cartographer.getUtil().getBlockType( snapshot, x, height, z );
				Color color = palette.getColor( material );
				if ( prevVal > 0 ) {
					if ( prevVal == height ) {
						color = JetpImageUtil.brightenColor( color, -10 );
					} else if ( prevVal > height ) {
						color = JetpImageUtil.brightenColor( color, -30 );
					}
				}
				
				data[ x + z * 16 ] = JetpImageUtil.getBestColor( color.getRGB() );
			}
		}
		
		return new ChunkData( data );
	}
}
