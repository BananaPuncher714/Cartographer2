package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import io.github.bananapuncher714.cartographer.core.map.ChunkDataProvider;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

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

}
