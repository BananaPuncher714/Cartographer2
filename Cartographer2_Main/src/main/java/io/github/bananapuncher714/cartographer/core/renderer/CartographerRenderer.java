package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

/**
 * Render a map and send the packet
 * 
 * @author BananaPuncher714
 */
public class CartographerRenderer extends MapRenderer {
	// Maximum number of ticks to keep updating the player after not recieving render calls for them
	private static final int UPDATE_THRESHOLD = 5000;
	
	// Async is not recommended, particularly because of the pixel and cursor providers
	private static final boolean ASYNC_RENDER = false;
	private static final boolean TICK_RENDER = true;
	
	private volatile boolean RUNNING = true;

	protected Thread renderer;

	protected Map< UUID, PlayerSetting > settings = new HashMap< UUID, PlayerSetting >();
	protected Map< UUID, Long > lastUpdated = new HashMap< UUID, Long >();
	
	protected int id;
	
	// Keep this a string in case if we delete a minimap, so that this doesn't store the map in memory
	protected String mapId;
	
	public CartographerRenderer( Minimap map ) {
		// Yes contextual
		super( true );

		if ( map != null ) {
			this.mapId = map.getId();
		} else {
			this.mapId = "MISSING";
		}
		
		// Allow multithreading for renderers? It would cause issues with synchronization, unfortunately
		if ( ASYNC_RENDER ) {
			settings = new ConcurrentHashMap< UUID, PlayerSetting >();
			renderer = new Thread( this::run );
			renderer.start();
		}
		if ( TICK_RENDER ) {
			// As it turns out, calling this is a lot more intensive than not
			Bukkit.getScheduler().runTaskTimer( Cartographer.getInstance(), this::tickRender, 20, Cartographer.getInstance().getRenderDelay() );
		}
	}
	
	private void run() {
		while ( RUNNING ) {
			update();
			try {
				Thread.sleep( 70 );
			} catch ( InterruptedException e ) {
			}
		}
	}
	
	private void update() {
		// Each person gets their own FrameRenderTask
		List< FrameRenderTask > tasks = new ArrayList< FrameRenderTask >();
		for ( Iterator< Entry< UUID, PlayerSetting > > iterator = settings.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< UUID, PlayerSetting > entry = iterator.next();
			
			// Stop updating people who aren't holding this map anymore, if it's been UPDATE_THRESHOLD ticks since they've last been called
			if ( System.currentTimeMillis() - lastUpdated.get( entry.getKey() ) > UPDATE_THRESHOLD ) {
				iterator.remove();
				continue;
			}

			// Make sure the player is online
			Player player = Bukkit.getPlayer( entry.getKey() );
			if ( player == null ) {
				iterator.remove();
				continue;
			}
			
			// Check if the minimap which they're trying to view actually exists
			PlayerSetting setting = entry.getValue();
			Minimap map = Cartographer.getInstance().getMapManager().getMinimaps().get( setting.map );
			if ( map == null ) {
				SimpleImage missingImage = Cartographer.getInstance().getMissingMapImage();
				byte[] missingMapData = JetpImageUtil.dither( missingImage.getWidth(), missingImage.getImage() );
				Cartographer.getInstance().getHandler().sendDataTo( id, missingMapData, null, entry.getKey() );
				continue;
			}
			
			// The map layers should look like this from top to bottom:
			// - Intermediate overlay, contains the MapPixels
			// - Global overlay - Depth of 0xFFFF, or 65535
			// - Lesser layer, contains the WorldMapPixels
			// - Map - Depth of 0
			// - Free real estate

			// Gather the cursors and pixels sync
			Collection< MapCursor > localCursors = map.getLocalCursorsFor( player, setting );
			Collection< WorldCursor > realWorldCursors = map.getCursorsFor( player, setting );
			Collection< MapPixel > pixels = map.getPixelsFor( player, setting );
			Collection< WorldPixel > worldPixels = map.getWorldPixelsFor( player, setting );
			
			MapDataCache cache = map.getDataCache();
			
			// Everything after this point can be done async
			RenderInfo renderInfo = new RenderInfo();
			renderInfo.setting = setting;
			renderInfo.uuid = player.getUniqueId();
			
			renderInfo.map = map;
			renderInfo.cache = cache;
			
			renderInfo.worldPixels = worldPixels;
			renderInfo.worldCursors = realWorldCursors;
			renderInfo.mapPixels = pixels;
			renderInfo.mapCursors = localCursors;
			
			// Create a new task per player and run
			FrameRenderTask task = new FrameRenderTask( renderInfo );
			tasks.add( task );
			
			task.fork();
		}
		
		// Once all the frames are done, then send
		for ( FrameRenderTask task : tasks ) {
			task.join();
			
			RenderInfo info = task.info;

			// Queue the locations that need loading
			for ( BigChunkLocation location : info.needsRender ) {
				info.map.getQueue().load( location );
			}
			
			// Send the packet
			byte[] data = info.data;
			MapCursor[] cursors = info.cursors;
			UUID uuid = info.uuid;
			Cartographer.getInstance().getHandler().sendDataTo( id, data, cursors, uuid );
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
	
	int tick = 0;
	
	// Since Paper only updates 4 times a tick, we'll have to compensate and manually update 20 times a tick instead
	private void tickRender() {
		// This is one of the most resource intensive methods
		// We'll have to disable this if the server is overloaded
		if ( Cartographer.getInstance().isServerOverloaded() ) {
			return;
		}
		
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
		} else if ( !TICK_RENDER ) {
			settings.get( player.getUniqueId() ).location = player.getLocation();
		}
		
		if ( !TICK_RENDER ) {
			if ( !ASYNC_RENDER ) {
				update();
			}
		}
	}

	public void terminate() {
		RUNNING = false;
	}
	
	public class RenderTask extends RecursiveTask< SubRenderInfo > {
		protected RenderInfo info;
		protected int index;
		protected int length;
		
		protected RenderTask( RenderInfo info, int index, int length ) {
			this.info = info;
			this.index = index;
			this.length = length;
		}
		
		@Override
		protected SubRenderInfo compute() {
			return null;
		}
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
