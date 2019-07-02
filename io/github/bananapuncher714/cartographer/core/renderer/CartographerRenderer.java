package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;

/**
 * 
 * 
 * @author BananaPuncher714
 */
public class CartographerRenderer extends MapRenderer {
	private static final boolean ASYNC_RENDER = false;
	
	volatile boolean RUNNING = true;

	protected Thread renderer;

	protected Map< UUID, PlayerSetting > settings = new HashMap< UUID, PlayerSetting >();
	
	protected int id;
	
	// Keep this a string in case if we delete a minimap, so that this doesn't store the map in memory
	protected String mapId;
	
	public CartographerRenderer( Minimap map ) {
		super( true );

		if ( map != null ) {
			this.mapId = map.getId();
		} else {
			this.mapId = "MISSING";
		}
		
		// Allow multithreading for renderers? It would cause issues with synchronization, unfortunately
		// Also, if enabled, be sure to make settings a concurrent hash map instead of a regular one
		if ( ASYNC_RENDER ) {
			renderer = new Thread( this::run );
			renderer.start();
		}
	}
	
	private void run() {
		while ( RUNNING ) {
			update();
			try {
				Thread.sleep( 50 );
			} catch ( InterruptedException e ) {
			}
		}
	}
	
	private void update() {
		for ( Iterator< Entry< UUID, PlayerSetting > > iterator = settings.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< UUID, PlayerSetting > entry = iterator.next();
			Player player = Bukkit.getPlayer( entry.getKey() );
			PlayerSetting setting = entry.getValue();
			Location loc = setting.location;
			loc.setY( loc.getWorld().getMaxHeight() - 1 );
			Minimap map = Cartographer.getInstance().getMapManager().getMinimaps().get( setting.map );
			if ( map == null ) {
				byte[] missingMapData = Cartographer.getInstance().getMissingMapImage();
				Cartographer.getInstance().getHandler().sendDataTo( id, missingMapData, null, entry.getKey() );
				continue;
			}
			
			MapDataCache cache = map.getDataCache();
			
			byte[] data = new byte[ 128 * 128 ];
			int[] overlay = new int[ 128 * 128 ];
			Location[] locations = MapUtil.getLocationsAround( loc, setting.zoomscale, setting.rotating ? Math.toRadians( loc.getYaw() + 180 ) : 0 );
			
			// Map Pixel color stuff
			Collection< MapPixel > pixels = map.getPixelsFor( player );
			for ( MapPixel pixel : pixels ) {
				int x = pixel.getX();
				int y = pixel.getZ();
				if ( x < 128 && x >= 0 && y < 128 && y >= 0 ) {
					int index = x + y * 128;
					int color = pixel.getColor().getRGB();
					if ( color >>> 24 == 0 ) {
						continue;
					}

					int prevColor = overlay[ index ];
					if ( prevColor >>> 24 == 0 ) {
						overlay[ index ] = color;
					} else {
						overlay[ index ] = JetpImageUtil.overwriteColor( color, prevColor );
					}
				}
				
			}
			
			int[] globalOverlay = Cartographer.getInstance().getOverlay();
			int[] loadingBackground = Cartographer.getInstance().getLoadingImage();
			// So right now we have overlay, which contains the intermediate layer of colors
			// The map layers should look like this from top to bottom:
			// - Global overlay
			// - Intermediate overlay
			// - Map
			// - Free real estate
			Set< BigChunkLocation > needsRender = new HashSet< BigChunkLocation >();
			for ( int index = 0; index < 128 * 128; index++ ) {
				int mapColor = 0;
				if ( globalOverlay != null ) {
					mapColor = globalOverlay[ index ];
				}
				
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColor( mapColor );
					continue;
				}
				
				int color = overlay[ index ];
				mapColor = JetpImageUtil.overwriteColor( color, mapColor );
				// Continue if the intermediate layer is opaque
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColor( mapColor );
					continue;
				}
				
				int loading = 0;
				if ( loadingBackground != null ) {
					loading = loadingBackground[ index ];
				}
				
				Location renderLoc = locations[ index ];
				// If renderLoc is null, we know it doesn't exist
				// Therefore, overwrite it with whatever color mapColor is
				if ( renderLoc == null ) {
					data[ index ] = JetpImageUtil.getBestColor( JetpImageUtil.overwriteColor( loading, mapColor ) );
					continue;
				}
				ChunkLocation cLocation = new ChunkLocation( renderLoc );
				int xOffset = renderLoc.getBlockX() - ( cLocation.getX() << 4 );
				int zOffset = renderLoc.getBlockZ() - ( cLocation.getZ() << 4 );

				ChunkData chunkData = cache.getDataAt( cLocation );

				int localColor = 0;
				if ( chunkData != null ) {
					// This is for static colors
//					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset, scale ) );
					// This is for dynamic colors
					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset ) );
				} else {
					if ( cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
						needsRender.add( new BigChunkLocation( cLocation ) );
					}
					
					localColor = loading;
				}
				
				mapColor = JetpImageUtil.overwriteColor( localColor, mapColor );
				
				data[ index ] = JetpImageUtil.getBestColor( mapColor );
			}
			for ( BigChunkLocation location : needsRender ) {
				map.getQueue().load( location );
			}
			
			double yawOffset = setting.rotating ? loc.getYaw() : 0;
			
			MapCursor[] cursors = null;
			Collection< MapCursor > localCursors = map.getLocalCursorsFor( player );
			Collection< RealWorldCursor > realWorldCursors = map.getCursorsFor( player );
			cursors = new MapCursor[ realWorldCursors.size() + localCursors.size() ];
			int index = 0;
			for ( RealWorldCursor cursor : realWorldCursors ) {
				Location cursorLoc = cursor.getLocation();
				double yaw = cursorLoc.getYaw() - yawOffset + 180;
				double relX = cursorLoc.getX() - loc.getX();
				double relZ = cursorLoc.getZ() - loc.getZ();
				double distance = Math.sqrt( relX * relX + relZ * relZ );

				double degree = Math.atan2( relZ, relX ) - Math.toRadians( yawOffset + 180 );
				double newRelX = 2 * distance * Math.cos( degree );
				double newRelZ = 2 * distance * Math.sin( degree );

				int normalizedX = ( int ) Math.min( 127, Math.max( -127, newRelX / setting.zoomscale ) );
				int normalizedZ = ( int ) Math.min( 127, Math.max( -127, newRelZ / setting.zoomscale ) );

				cursors[ index++ ] = Cartographer.getInstance().getHandler().constructMapCursor( normalizedX, normalizedZ, yaw, cursor.getType(), cursor.getName() );
			}
			for ( MapCursor cursor : localCursors ) {
				cursors[ index++ ] = cursor;
			}
			
			Cartographer.getInstance().getHandler().sendDataTo( id, data, cursors, entry.getKey() );
		}
	}

	public void setPlayerMap( Player player, Minimap map ) {
		PlayerSetting setting = new PlayerSetting( map.getId(), player.getLocation() );
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
	
	public Minimap getMinimap() {
		return Cartographer.getInstance().getMapManager().getMinimaps().get( mapId );
	}
	
	public void setMinimap( Minimap map ) {
		for ( PlayerSetting setting : settings.values() ) {
			setting.map = map.getId();
		}
		this.mapId = map.getId();
	}
	
	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		id = view.getId();

		if ( settings.containsKey( player.getUniqueId() ) ) {
			settings.get( player.getUniqueId() ).location = player.getLocation();
		} else {
			PlayerSetting setting = new PlayerSetting( mapId, player.getLocation() );
			settings.put( player.getUniqueId(), setting );
		}
		
		if ( !ASYNC_RENDER ) {
			update();
		}
	}

	public void terminate() {
		RUNNING = false;
	}
	
	protected class PlayerSetting {
		protected Location location;
		protected double zoomscale = 1;
		protected String map;
		protected boolean rotating = true;
		
		public PlayerSetting( String map, Location location ) {
			this.map = map;
			this.location = location;
		}
		
		public PlayerSetting setScale( double scale ) {
			this.zoomscale = scale;
			return this;
		}
	}
}
