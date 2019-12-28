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
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.RealWorldCursor;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;

/**
 * Render a map and send the packet
 * 
 * @author BananaPuncher714
 */
public class CartographerRenderer extends MapRenderer {
	// Maximum number of ticks to keep updating the player after not recieving render calls for them
	private static final int UPDATE_THRESHOLD = 5000;
	private static final boolean ASYNC_RENDER = false;
	
	private volatile boolean RUNNING = true;

	protected Thread renderer;

	protected Map< UUID, PlayerSetting > settings = new HashMap< UUID, PlayerSetting >();
	protected Map< UUID, Long > lastUpdated = new HashMap< UUID, Long >();
	
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
		} else {
			Bukkit.getScheduler().runTaskTimer( Cartographer.getInstance(), this::tickRender, 20, 1 );
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
			
			// Stop updating people who aren't holding this map anymore
			if ( System.currentTimeMillis() - lastUpdated.get( entry.getKey() ) > UPDATE_THRESHOLD ) {
				iterator.remove();
				continue;
			}
			
			Player player = Bukkit.getPlayer( entry.getKey() );
			
			if ( player == null ) {
				iterator.remove();
				continue;
			}
			
			PlayerSetting setting = entry.getValue();
			Location loc = setting.location;
			loc.setY( loc.getWorld().getMaxHeight() - 1 );
			Minimap map = Cartographer.getInstance().getMapManager().getMinimaps().get( setting.map );
			if ( map == null ) {
				SimpleImage missingImage = Cartographer.getInstance().getMissingMapImage();
				byte[] missingMapData = JetpImageUtil.dither( missingImage.getWidth(), missingImage.getImage() );
				Cartographer.getInstance().getHandler().sendDataTo( id, missingMapData, null, entry.getKey() );
				continue;
			}
			
			MapDataCache cache = map.getDataCache();
			
			byte[] data = new byte[ 128 * 128 ];
			int[] higherMapPixels = new int[ 128 * 128 ];
			int[] lowerMapPixels = new int[ 128 * 128 ];
			
			BooleanOption rotation = map.getSettings().getRotation();
			boolean rotating = rotation == BooleanOption.DEFAULT ? setting.rotating : ( rotation == BooleanOption.ON ? true : false );
			
			// Collect all the locations that need their color fetched
			Location[] locations = MapUtil.getLocationsAround( loc, setting.zoomscale, rotating ? Math.toRadians( loc.getYaw() + 540 ) : 0 );
			
			// Map Pixel color stuff
			Collection< MapPixel > pixels = map.getPixelsFor( player, setting );
			for ( Iterator< MapPixel > pixelIterator = pixels.iterator(); pixelIterator.hasNext(); ) {
				MapPixel pixel = pixelIterator.next();
				int x = pixel.getX();
				int y = pixel.getZ();
				if ( x < 128 && x >= 0 && y < 128 && y >= 0 ) {
					int index = x + y * 128;
					int color = pixel.getColor().getRGB();
					if ( color >>> 24 == 0 ) {
						continue;
					}

					if ( pixel.getDepth() < 0xFFFF ) {
						int prevColor = lowerMapPixels[ index ];
						// These go under the overlay
						lowerMapPixels[ index ] = JetpImageUtil.overwriteColor( prevColor, color );
					} else {
						int prevColor = higherMapPixels[ index ];
						// Add the colors on top of the overlay. The pixels provided have priority
						higherMapPixels[ index ] = JetpImageUtil.overwriteColor( prevColor, color );
					}
				}
			}
			
			Collection< WorldPixel > worldPixels = map.getWorldPixelsFor( player, setting );
			
			int[] globalOverlay = Cartographer.getInstance().getOverlay().getImage();
			int[] loadingBackground = Cartographer.getInstance().getLoadingImage().getImage();
			// So right now we have overlay, which contains the intermediate layer of colors
			// The map layers should look like this from top to bottom:
			// - Intermediate overlay, contains the MapPixels
			// - Global overlay - Depth of 0xFFFF, or 65535
			// - Lesser layer, contains the WorldMapPixels
			// - Map - Depth of 0
			// - Free real estate
			Set< BigChunkLocation > needsRender = new HashSet< BigChunkLocation >();
			for ( int index = 0; index < 128 * 128; index++ ) {
				int mapColor = 0;
				
				// Get the custom map pixel
				int color = higherMapPixels[ index ];
				// Continue if the pixel is opaque, since we know that nothing else be above this
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
					continue;
				} else {
					// Otherwise, we want to set it as the bottom layer
					mapColor = color;
				}
				
				// Then the global overlay
				// The global overlay is still the background to the foreground
				if ( globalOverlay != null ) {
					mapColor = JetpImageUtil.overwriteColor( globalOverlay[ index ], mapColor );
				}
				
				// See if the global overlay is opaque
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
					continue;
				}
				
				int lowerColor = lowerMapPixels[ index ];
				mapColor = JetpImageUtil.overwriteColor( lowerColor, mapColor );
				
				// See if the pixels are opaque
				if ( mapColor >>> 24 == 0xFF ) {
					data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
					continue;
				}
				
				// Then get the loading background
				int loading = 0;
				if ( loadingBackground != null ) {
					loading = loadingBackground[ index ];
				}
				
				// The render location comes next
				Location renderLoc = locations[ index ];
				// If renderLoc is null, we know it doesn't exist
				// Therefore, overwrite it with whatever color mapColor is
				if ( renderLoc == null ) {
					data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( JetpImageUtil.overwriteColor( loading, mapColor ) );
					continue;
				}
				
				// If not, then we try and get the render location
				ChunkLocation cLocation = new ChunkLocation( renderLoc );
				int xOffset = renderLoc.getBlockX() - ( cLocation.getX() << 4 );
				int zOffset = renderLoc.getBlockZ() - ( cLocation.getZ() << 4 );

				ChunkData chunkData = cache.getDataAt( cLocation );

				int localColor = 0;
				if ( chunkData != null ) {
					// This is for static colors
//					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset, setting.getScale() ) );
					// This is for dynamic colors
					localColor = JetpImageUtil.getColorFromMinecraftPalette( chunkData.getDataAt( xOffset, zOffset ) );
				} else {
					if ( cache.requiresGeneration( cLocation ) && !ChunkLoadListener.isLoading( cLocation ) ) {
						needsRender.add( new BigChunkLocation( cLocation ) );
					}
					
					localColor = loading;
				}
				
				// First, insert any WorldPixels that may be present
				for ( WorldPixel pixel : worldPixels ) {
					if ( renderLoc.getWorld() == player.getWorld() && pixel.intersects( renderLoc.getX(), renderLoc.getZ() ) ) {
						localColor = JetpImageUtil.overwriteColor( localColor, pixel.getColor().getRGB() );
					}
				}
				
				// Then, get the color and mix it under the current overlay color
				mapColor = JetpImageUtil.overwriteColor( localColor, mapColor );
				mapColor = JetpImageUtil.overwriteColor( loading, mapColor );
				
				data[ index ] = JetpImageUtil.getBestColorIncludingTransparent( mapColor );
			}
			for ( BigChunkLocation location : needsRender ) {
				map.getQueue().load( location );
			}
			
			double yawOffset = setting.rotating ? loc.getYaw() + 180 : 0;
			
			MapCursor[] cursors = null;
			Collection< MapCursor > localCursors = map.getLocalCursorsFor( player, setting );
			Collection< RealWorldCursor > realWorldCursors = map.getCursorsFor( player, setting );
			cursors = new MapCursor[ realWorldCursors.size() + localCursors.size() ];
			int index = 0;
			for ( RealWorldCursor cursor : realWorldCursors ) {
				Location cursorLoc = cursor.getLocation();
				double yaw = cursorLoc.getYaw() - yawOffset + 720;
				double relX = cursorLoc.getX() - loc.getX();
				double relZ = cursorLoc.getZ() - loc.getZ();
				double distance = Math.sqrt( relX * relX + relZ * relZ );

				double degree = Math.atan2( relZ, relX ) - Math.toRadians( yawOffset );
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
			if ( map == null ) {
				setting.map = null;
			} else {
				setting.map = map.getId();
			}
		}
		this.mapId = map == null ? null : map.getId();
	}
	
	// Since Paper only updates 4 times a tick, we'll have to compensate and manually update 20 times a tick instead
	private void tickRender() {
		for ( Iterator< Entry< UUID, PlayerSetting > > iterator = settings.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< UUID, PlayerSetting > entry = iterator.next();
			UUID uuid = entry.getKey();
			Player player = Bukkit.getPlayer( uuid );
			
			if ( player == null ) {
				iterator.remove();
				continue;
			}
			
			PlayerSetting setting = entry.getValue();
			setting.location = player.getLocation();
		}
		
		if ( !ASYNC_RENDER ) {
			update();
		}
	}
	
	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		lastUpdated.put( player.getUniqueId(), System.currentTimeMillis() );
		id = Cartographer.getUtil().getId( view );

		if ( !settings.containsKey( player.getUniqueId() ) ) {
			PlayerSetting setting = new PlayerSetting( mapId, player.getLocation() );
			setting.rotating = Cartographer.getInstance().isRotateByDefault();
			settings.put( player.getUniqueId(), setting );
		}
	}

	public void terminate() {
		RUNNING = false;
	}
	
	public class PlayerSetting {
		protected Location location;
		protected double zoomscale = 1;
		protected String map;
		protected boolean rotating = true;
		
		protected PlayerSetting( String map, Location location ) {
			this.map = map;
			this.location = location;
		}
		
		public PlayerSetting setScale( double scale ) {
			this.zoomscale = scale;
			return this;
		}
		
		public boolean isRotating() {
			return rotating;
		}
		
		public String getMap() {
			return map;
		}
		
		public double getScale() {
			return zoomscale;
		}
		
		public Location getLocation() {
			return location.clone();
		}
	}
}
