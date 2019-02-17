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
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
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
	volatile boolean RUNNING = true;

	protected Thread renderer;

	protected Map< UUID, PlayerSetting > settings = new ConcurrentHashMap< UUID, PlayerSetting >();
	
	protected int id;
	
	public CartographerRenderer() {
		super( true );
		renderer = new Thread( this::run );
		renderer.start();
	}
	
	private void run() {
		while ( RUNNING ) {
			for ( Iterator< Entry< UUID, PlayerSetting > > iterator = settings.entrySet().iterator(); iterator.hasNext(); ) {
				Entry< UUID, PlayerSetting > entry = iterator.next();
				PlayerSetting setting = entry.getValue();
				Location loc = setting.location;
				loc.setY( loc.getWorld().getMaxHeight() - 1 );
				Minimap map = setting.map;
				MapDataCache cache = map.getDataCache();
				
				byte[] data = new byte[ 128 * 128 ];
				Location[] locations = MapUtil.getLocationsAround( loc, setting.zoomscale, setting.rotating ? Math.toRadians( loc.getYaw() + 180 ) : 0 );
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
						// This is for static colors
//						data[ index ] = chunkData.getDataAt( xOffset, zOffset, scale );
						// This is for dynamic colors
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
			}
			try {
				Thread.sleep( 50 );
			} catch ( InterruptedException e ) {
			}
		}
	}

	public void setPlayerMap( Player player, Minimap map ) {
		PlayerSetting setting = new PlayerSetting( map, player.getLocation() );
		if ( settings.containsKey( player.getUniqueId() ) ) {
			setting.zoomscale = settings.get( player.getUniqueId() ).zoomscale;
		} else {
			setting.zoomscale = map.getSettings().getDefaultZoom().getBlocksPerPixel();
		}
		settings.put( player.getUniqueId(), setting );
	}
	
	public void setRotatingFor( UUID uuid, boolean rotating ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting != null ) {
			setting.rotating = rotating;
		}
	}
	
	public boolean isRotating( UUID uuid ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting == null ) {
			return false;
		}
		return setting.rotating;
	}
	
	public ZoomScale getScale( UUID uuid ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting == null ) {
			return null;
		}
		return ZoomScale.getScale( setting.zoomscale );
	}
	
	public void setScale( UUID uuid, ZoomScale scale ) {
		setScale( uuid, scale.getBlocksPerPixel() );
	}
	
	public void setScale( UUID uuid, double blocksPerPixel ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting != null ) {
			setting.setScale( blocksPerPixel );
		}
	}
	
	public boolean isViewing( UUID uuid ) {
		return settings.containsKey( uuid );
	}
	
	public void unregisterPlayer( Player player ) {
		settings.remove( player.getUniqueId() );
	}
	
	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		id = view.getId();

		if ( settings.containsKey( player.getUniqueId() ) ) {
			settings.get( player.getUniqueId() ).location = player.getLocation();
		} else {
			Minimap map = Cartographer.getInstance().getMapManager().getCurrentMap( player.getUniqueId() );

			if ( map == null ) {
				return;
			}

			PlayerSetting setting = new PlayerSetting( map, player.getLocation() );
			settings.put( player.getUniqueId(), setting );
		}
	}

	public void terminate() {
		RUNNING = false;
	}
	
	protected class PlayerSetting {
		protected Location location;
		protected double zoomscale = 1;
		protected Minimap map;
		protected boolean rotating = true;
		
		public PlayerSetting( Minimap map, Location location ) {
			this.map = map;
			this.location = location;
		}
		
		public PlayerSetting setScale( double scale ) {
			this.zoomscale = scale;
			return this;
		}
	}
}
