package io.github.bananapuncher714.cartographer.core.renderer;

import java.awt.Color;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;

public class CartographerRenderer extends MapRenderer {
	public static final int SIZE = 1;

	volatile boolean RUNNING = true;

	Thread renderer;

	MapDataCache cache = new MapDataCache( new ChunkDataProvider() {
		@Override
		public ChunkData process( ChunkSnapshot snapshot ) {
			int[] buffer = new int[ 16 ];
			ChunkLocation north = new ChunkLocation( snapshot ).setZ( snapshot.getZ() - 1 );
			ChunkSnapshot northSnapshot = chunks.get( north );
			if ( northSnapshot == null ) {
				registryLock.lock();
				rendering.remove( north.setZ( north.getZ() + 1 ) );
				registryLock.unlock();
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
					Material material = snapshot.getBlockData( x, height, z ).getMaterial();
					Color color = palette.getColor( material );
					if ( material == Material.WATER ) {
						int depth = BlockUtil.getWaterDepth( snapshot, x, height, z );
						double percentage = Math.min( 1, depth / 64.0 );
						color = JetpImageUtil.brightenColor( color, ( int ) ( percentage * - 100 ) );
					} else if ( prevVal > 0 ) {
						if ( prevVal == height ) {
							color = JetpImageUtil.brightenColor( color, -10 );
						} else if ( prevVal > height ) {
							color = JetpImageUtil.brightenColor( color, -30 );
						}
					}
					
					data[ x + z * 16 ] = JetpImageUtil.getBestColor( color.getRGB() );
				}
			}

			if ( cache.containsDataAt( north ) ) {
				chunks.remove( north );
			}
			if ( cache.containsDataAt( north.setZ( north.getZ() + 2 ) ) && chunks.containsKey( north ) ) {
				chunks.remove( north.setZ( north.getZ() - 1 ) );
			}
			
			return new ChunkData( data );
		}
	} );

	Map< ChunkLocation, ChunkSnapshot > chunks = new ConcurrentHashMap< ChunkLocation, ChunkSnapshot >();

	Set< ChunkLocation > registered = new HashSet< ChunkLocation >();
	Set< ChunkLocation > rendering = new HashSet< ChunkLocation >();
	
	MinimapPalette palette = Cartographer.getInstance().getPalette();
	
	int id;
	UUID uuid;
	Location currentLoc;

	ReentrantLock lock = new ReentrantLock();
	ReentrantLock registryLock = new ReentrantLock();

	public CartographerRenderer() {
		renderer = new Thread( this::run );
		renderer.start();
	}

	private void run() {
		while ( RUNNING ) {
			if ( currentLoc != null ) {
				lock.lock();
				Location loc = currentLoc.clone();
				lock.unlock();
				if ( loc != null ) {
					byte[] data = new byte[ 128 * 128 ];
					Location[] locations = MapUtil.getLocationsAround( loc.add( 91, 0, 91 ), .5, Math.toRadians( loc.getYaw() + 180 ) );
					for ( int index = 0; index < 128 * 128; index++ ) {
						Location renderLoc = locations[ index ];
						if ( renderLoc == null ) {
							continue;
						}
						ChunkLocation cLocation = new ChunkLocation( renderLoc );
						int xOffset = renderLoc.getBlockX() - ( cLocation.getX() << 4 );
						int zOffset = renderLoc.getBlockZ() - ( cLocation.getZ() << 4 );
	
						ChunkData chunkData = cache.getDataAt( cLocation );
	
						registryLock.lock();
						if ( chunkData != null ) {
							registryLock.unlock();
							data[ index ] = chunkData.getData()[ xOffset + zOffset * 16 ];
						} else if ( !rendering.contains( cLocation ) ) {
							ChunkSnapshot snapshot = chunks.get( cLocation );
	
							if ( snapshot == null ) {
								continue;
							}
	
							rendering.add( cLocation );
							registryLock.unlock();
							cache.process( snapshot );
						}
					}
					Cartographer.getInstance().getHandler().sendDataTo( id, data, null, uuid );
				}
			}
			try {
				Thread.sleep( 25 );
			} catch ( InterruptedException e ) {
			}
		}
	}

	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		if ( uuid == null ) {
			uuid = player.getUniqueId();
			id = view.getId();
		}

		Location location = player.getLocation().subtract( 107, 0, 107 );
		int x = location.getChunk().getX();
		int z = location.getChunk().getZ();
		for ( int xo = 0; xo < 14; xo++) {
			for ( int zo = 0; zo < 14; zo++ ) {
				ChunkLocation cLocation = new ChunkLocation( location.getWorld(), x + xo, z + zo );
				if ( !registered.contains( cLocation ) ) {
					chunks.put( cLocation, cLocation.getChunk().getChunkSnapshot() );
					registered.add( cLocation );
				}
			}
		}

		location.setY( location.getWorld().getMaxHeight() - 1 );
		lock.lock();
		currentLoc = location.add( 16, 0, 16 );
		lock.unlock();
	}

	public void terminate() {
		RUNNING = false;
	}
}
