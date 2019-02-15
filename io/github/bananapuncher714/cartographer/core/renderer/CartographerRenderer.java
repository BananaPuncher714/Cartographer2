package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;

/**
 * 
 * 
 * @author BananaPuncher714
 */
public class CartographerRenderer extends MapRenderer {
	protected double scale = 1;
	
	volatile boolean RUNNING = true;

	protected Thread renderer;

	protected Map< UUID, Location > locations = new ConcurrentHashMap< UUID, Location >();
	
	protected Minimap map;
	protected MapDataCache cache;
	protected MinimapPalette palette = Cartographer.getInstance().getPalette();
	
	protected int id;

	protected boolean rotating = true;
	
	public CartographerRenderer( Minimap map ) {
		this.map = map;
		this.cache = map.getDataCache();
		renderer = new Thread( this::run );
		renderer.start();
	}

	public void setRotating( boolean rotating ) {
		this.rotating = rotating;
	}
	
	public boolean isRotating() {
		return rotating;
	}
	
	public void setScale( double scale ) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	
	private void run() {
		while ( RUNNING ) {
			for ( Iterator< Entry< UUID, Location > > iterator = locations.entrySet().iterator(); iterator.hasNext(); ) {
				Entry< UUID, Location > entry = iterator.next();
				Location loc = entry.getValue();
				
				byte[] data = new byte[ 128 * 128 ];
				Location[] locations = MapUtil.getLocationsAround( loc, scale, rotating ? Math.toRadians( loc.getYaw() + 180 ) : 0 );
				Set< BigChunkLocation > needsRender = new HashSet< BigChunkLocation >();
				for ( int index = 0; index < 128 * 128; index++ ) {
					Location renderLoc = locations[ index ];
					if ( renderLoc == null ) {
						continue;
					}
					ChunkLocation cLocation = new ChunkLocation( renderLoc );
					int xOffset = renderLoc.getBlockX() - ( cLocation.getX() << 4 );
					int zOffset = renderLoc.getBlockZ() - ( cLocation.getZ() << 4 );

					ChunkData chunkData = cache.getDataAt( cLocation );

					if ( chunkData != null ) {
//						data[ index ] = chunkData.getDataAt( xOffset, zOffset, scale );
						data[ index ] = chunkData.getDataAt( xOffset, zOffset );
//						if ( ChunkLoadListener.isLoading( cLocation ) ) {
//							// Red
//							// The ChunkLoadListener is re-loading a pre-existing chunk
//							data[ index ] = 17;
//						}
					} else if ( cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
						needsRender.add( new BigChunkLocation( cLocation ) );
					} else {
						if ( ChunkLoadListener.isLoading( cLocation ) ) {
							// Gray
							// The ChunkLoadListener is going to load a chunk that does not exist
							data[ index ] = 13;
						}
					}
//					if ( cache.hasSnapshot( cLocation ) ) {
//						if ( chunkData != null ) {
//							// Green
//							// Means that the cache has yet to purge the unused chunk snapshots
//							data[ index ] = 7;
//						} else {
//							// Yellow-brown
//							// The cache needs to process the chunks
//							data[ index ] = 120;
//						}
//					}
				}
				for ( BigChunkLocation location : needsRender ) {
					map.getQueue().load( location );
				}
				Cartographer.getInstance().getHandler().sendDataTo( id, data, new MapCursor[] { new MapCursor( ( byte ) 0, ( byte ) 0, ( byte ) 8, Type.WHITE_POINTER, true, null ) }, entry.getKey() );
				iterator.remove();
			}
			try {
				Thread.sleep( 50 );
			} catch ( InterruptedException e ) {
			}
		}
	}

	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		id = view.getId();

		Location location = player.getLocation();
		location.setY( location.getWorld().getMaxHeight() - 1 );
		
		locations.put( player.getUniqueId(), location );
	}

	public void terminate() {
		RUNNING = false;
	}
}
