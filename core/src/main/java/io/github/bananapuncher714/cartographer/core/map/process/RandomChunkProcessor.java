package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

/**
 * Fill the map with noise.
 * 
 * @author BananaPuncher714
 */
public class RandomChunkProcessor implements ChunkDataProvider {
	protected NoiseGenerator generator = new SimplexNoiseGenerator( 1337 );
	
	@Override
	public ChunkData process( ChunkSnapshot snapshot ) {
		double cx = ( snapshot.getX() << 4 );
		double cz = ( snapshot.getZ() << 4 );
		
		byte[] data = new byte[ 256 ];
		for ( int x = 0; x < 16; x++ ) {
			for ( int z = 0; z < 16; z++ ) {
				double dx = ( cx + x ) * .0125;
				double dz = ( cz + z ) * .0125;
				
				double noise = ( generator.noise( dx, dz ) + 1 ) / 2.0;
				int rgb = ( int ) ( noise * JetpImageUtil.getLargestColorVal() );
				
				data[ x + z * 16 ] = ( byte ) rgb;
			}
		}
		
		return new ChunkData( data );
	}

	@Override
	public int process( Location location, MinimapPalette palette ) {
		double dx = location.getX() * .0125;
		double dz = location.getZ() * .0125;
		
		double noise = ( generator.noise( dx, dz ) + 1 ) / 2.0;
		int rgb = ( int ) ( noise * JetpImageUtil.getLargestColorVal() );
		
		return JetpImageUtil.getColorFromMinecraftPalette( ( byte ) rgb ) ;
	}
	
}
