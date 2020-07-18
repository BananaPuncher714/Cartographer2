package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.api.events.renderer.CartographerRendererActivateEvent;
import io.github.bananapuncher714.cartographer.core.api.events.renderer.CartographerRendererDeactivateEvent;
import io.github.bananapuncher714.cartographer.core.api.events.renderer.CartographerRendererDisabledEvent;
import io.github.bananapuncher714.cartographer.core.api.events.renderer.CartographerRendererInteractEvent;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache.ChunkState;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;
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

	protected Map< UUID, Double > scales = new HashMap< UUID, Double >();
	protected Map< UUID, PlayerSetting > settings = new HashMap< UUID, PlayerSetting >();
	
	protected Cartographer plugin;
	
	protected int id;
	
	// Keep this a string in case if we delete a minimap, so that this doesn't store the map in memory
	protected String mapId = null;
	
	protected long tick = 0;
	
	public CartographerRenderer( Cartographer plugin, Minimap map ) {
		// Yes contextual
		super( true );

		this.plugin = plugin;
		if ( map != null ) {
			this.mapId = map.getId();
		}
		
		// Allow multithreading for renderers? It would cause issues with synchronization, unfortunately
		if ( ASYNC_RENDER ) {
			settings = new ConcurrentHashMap< UUID, PlayerSetting >();
			renderer = new Thread( this::run );
			renderer.start();
		}
		if ( TICK_RENDER ) {
			// As it turns out, calling this is a lot more intensive than not
			Bukkit.getScheduler().runTaskTimer( plugin, this::tickRender, 20, 1 );
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
			PlayerSetting setting = entry.getValue();
			scales.put( entry.getKey(), setting.getScale() );
			
			// Stop updating people who aren't holding this map anymore, if it's been UPDATE_THRESHOLD ticks since they've last been called
			if ( System.currentTimeMillis() - setting.lastUpdated > UPDATE_THRESHOLD ) {
				new CartographerRendererDeactivateEvent( entry.getKey(), this ).callEvent();
				
				setting.deactivate();
				iterator.remove();
				continue;
			}

			// Make sure the player is online
			Player player = Bukkit.getPlayer( entry.getKey() );
			if ( player == null ) {
				new CartographerRendererDeactivateEvent( entry.getKey(), this ).callEvent();
				
				setting.deactivate();
				iterator.remove();
				continue;
			}
			
			// If the player is currently engaged in map data
			MapMenu menu = setting.menu;
			if ( menu != null ) {
				boolean close = menu.view( player, setting );
				if ( close ) {
					menu.onClose( entry.getKey() );
					setting.menu = null;
				} else {
					byte[] data = menu.getDisplay();
					
					Type type = FailSafe.getEnum( Type.class, "SMALL_WHITE_CIRCLE", "WHITE_CIRCLE", "WHITE_CROSS" );
					
					int x = ( int ) Math.max( -128, Math.min( 127, setting.getCursorX() ) );
					int y = ( int ) Math.max( -128, Math.min( 127, setting.getCursorY() ) );
					
					List< MapCursor > cursors = new ArrayList< MapCursor >( setting.menu.getCursors() );
					MapCursor cursor = Cartographer.getInstance().getHandler().constructMapCursor( x, y, 0, type, null );
					cursors.add( cursor );
					
					plugin.getHandler().sendDataTo( id, data, cursors.toArray( new MapCursor[ cursors.size() ] ), entry.getKey() );
				}
				continue;
			}
			
			// Check if the minimap which they're trying to view actually exists
			Minimap map = setting.map == null ? null : plugin.getMapManager().getMinimaps().get( setting.map );
			if ( map == null ) {
				SimpleImage missingImage = plugin.getMissingMapImage();
				byte[] missingMapData;
				if ( plugin.isDitherMissingMapImage() ) {
					missingMapData = JetpImageUtil.dither2Minecraft( missingImage.getImage(), missingImage.getWidth() ).array();
				} else {
					missingMapData = JetpImageUtil.simplify( missingImage.getImage() );
				}
				plugin.getHandler().sendDataTo( id, missingMapData, null, entry.getKey() );
				continue;
			}
			
			if ( map.getSettings().isBlacklisted( setting.getLocation().getWorld().getName() ) ) {
				SimpleImage image = map.getDisabledImage();
				if ( image == null ) {
					image = plugin.getDisabledMapImage();
				}
				byte[] data = new byte[ 128 * 128 ];
				if ( image != null ) {
					if ( map.getSettings().isDitherBlacklisted() ) {
						data = JetpImageUtil.dither2Minecraft( image.getImage(), image.getWidth() ).array();
					} else {
						data = JetpImageUtil.simplify( image.getImage() );
					}
				}
				CartographerRendererDisabledEvent event = new CartographerRendererDisabledEvent( this, data );
				event.callEvent();
				data = event.getData();
				
				plugin.getHandler().sendDataTo( id, data, null, entry.getKey() );
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

			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			
			SimpleImage overlayImage = plugin.getOverlay();
			if ( map.getOverlayImage() != null ) {
				overlayImage = map.getOverlayImage();
			} else if ( viewer.getOverlay() != null ) {
				overlayImage = viewer.getOverlay();
			}
			
			SimpleImage backgroundImage = plugin.getBackground();
			if ( map.getBackgroundImage() != null ) {
				backgroundImage = map.getBackgroundImage();
			} else if ( viewer.getBackground() != null ) {
				backgroundImage = viewer.getBackground();
			}
			
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
			
			renderInfo.overlayImage = overlayImage;
			renderInfo.backgroundImage = backgroundImage;
			
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
				// TODO Check if the location needs loading and load somewhere else maybe?
				/*
				 *  TODO Keep track of the current state of the chunk data
				 * A chunk can have one of the following states:
				 * NONE						The chunk is not being processed, nor has it undergone processing
				 * WAITING_FOR_LOAD			The chunk is waiting to be loaded from the vanilla world
				 * WAITING_FOR_PROCESSING	The chunk is waiting to be processed
				 * PROCESSING				The chunk is currently being processed
				 * CACHED					The chunk has been cached
				 * WAITING_FOR_FILE_LOAD	The chunk is waiting to be loaded in from Cartographer2 local files
				 * FILE_LOADING				The chunk is being loaded in from Cartographer2 files
				 * WAITING_FOR_SAVING		The chunk is waiting to be saved to file
				 * SAVING					The chunk is being saved to file
				 */
				int cx = location.getX() << 4;
				int cz = location.getZ() << 4;
				boolean renderRequired = false;
				for ( int x = 0; x < 16 && !renderRequired; x++ ) {
					for ( int z = 0; z < 16 && !renderRequired; z++ ) {
						ChunkLocation cLocation = new ChunkLocation( location.getWorld(), cx + x, cz + z );
						// Check if this location has a state of NONE
						ChunkState state = MapDataCache.getStateOf( info.map, cLocation );
						
						// Add this location only if a chunk isn't cached or involved in anything
						renderRequired = state == ChunkState.NONE;
					}
				}
				if ( renderRequired ) {
					// Try to load it from file first
					info.map.getQueue().load( location );
				}
			}
			
			// Send the packet
			byte[] data = info.data;
			MapCursor[] cursors = info.cursors;
			UUID uuid = info.uuid;
			plugin.getHandler().sendDataTo( id, data, cursors, uuid );
		}
		
		// Remove the player interacted flag
		for ( PlayerSetting setting : settings.values() ) {
			setting.interaction = null;
		}
	}

	public boolean setPlayerMap( Player player, Minimap map ) {
		PlayerSetting setting = settings.get( player.getUniqueId() );
		if ( setting != null ) {
			setting.map = map == null ? null : map.getId();
			setting.zoomscale = map.getSettings().getDefaultZoom();
			return true;
		}
		return false;
	}
	
	public double getScale( UUID uuid ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting == null ) {
			return plugin.getMapManager().getMinimaps().get( mapId ).getSettings().getDefaultZoom();
		}
		return setting.getScale();
	}
	
	public void setScale( UUID uuid, ZoomScale scale ) {
		setScale( uuid, scale.getBlocksPerPixel() );
	}
	
	public void setScale( UUID uuid, double blocksPerPixel ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting != null ) {
			setting.setScale( blocksPerPixel );
		}
		scales.put( uuid, blocksPerPixel );
	}
	
	public void setMapMenu( UUID uuid, MapMenu menu ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting != null ) {
			MapMenu oldMenu = setting.getMenu();
			if ( oldMenu != null ) {
				oldMenu.onClose( uuid );
			}
			setting.menu = menu;
		}
	}
	
	public MapMenu getMenu( UUID uuid ) {
		PlayerSetting setting = settings.get( uuid );
		if ( setting != null ) {
			return setting.getMenu();
		}
		return null;
	}
	
	public Set< UUID > getActiveMapMenuViewers() {
		Set< UUID > viewers = new HashSet< UUID >();
		
		for ( UUID uuid : settings.keySet() ) {
			PlayerSetting setting = settings.get( uuid );
			
			if ( setting.getMenu() != null ) {
				viewers.add( uuid );
			}
		}
		
		return viewers;
	}
	
	public void interact( Player player, MapInteraction interaction ) {
		if ( interaction == MapInteraction.LEFT ) {
			// Disregard left clicks since they trigger when a player presses 'Q' as well.
//			return;
		}
		
		PlayerSetting setting = settings.get( player.getUniqueId() );
		if ( setting != null ) {
			setting.interaction = interaction;
			
			MapMenu menu = setting.getMenu();
			if ( menu != null ) {
				CartographerRendererInteractEvent event = new CartographerRendererInteractEvent( player, this, menu, interaction );
				event.callEvent();
				if ( !event.isCancelled() ) {
					if ( menu.interact( player, setting ) ) {
						menu.onClose( player.getUniqueId() );
						setting.menu = null;
					}
				}
			}
		}
	}
	
	public boolean isViewing( UUID uuid ) {
		return settings.containsKey( uuid );
	}
	
	public void unregisterPlayer( Player player ) {
		settings.remove( player.getUniqueId() );
	}
	
	public Minimap getMinimap() {
		return mapId == null ? null : plugin.getMapManager().getMinimaps().get( mapId );
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
	
	public void resetCursorFor( Player player ) {
		if ( settings.containsKey( player.getUniqueId() ) ) {
			Location location = player.getLocation();
			PlayerSetting setting = settings.get( player.getUniqueId() );
			setting.setCursorX( 0 );
			setting.setCursorY( 0 );
			setting.setCursorYaw( ( ( location.getYaw() % 360 ) + 360 ) % 360 );
		}
	}
	
	// Since Paper only updates 4 times a tick, we'll have to compensate and manually update 20 times a tick instead
	private void tickRender() {
		// This is one of the most resource intensive methods
		// We'll have to disable this if the server is overloaded
		if ( plugin.isServerOverloaded() ) {
			return;
		}
		// Render once ever X ticks
		if ( tick++ % plugin.getRenderDelay() != 0 ) {
			return;
		}
		
		for ( Iterator< Entry< UUID, PlayerSetting > > iterator = settings.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< UUID, PlayerSetting > entry = iterator.next();
			UUID uuid = entry.getKey();
			Player player = Bukkit.getPlayer( uuid );
			PlayerSetting setting = entry.getValue();
			
			if ( player == null ) {
				new CartographerRendererDeactivateEvent( entry.getKey(), this ).callEvent();
				
				setting.deactivate();
				iterator.remove();
				continue;
			}
			
			ItemStack main = Cartographer.getUtil().getMainHandItem( player );
			ItemStack off = Cartographer.getUtil().getOffHandItem( player );
			
			boolean inHand = false;
			boolean mainHand = false;
			if ( main != null ) {
				MapView mainView = Cartographer.getUtil().getMapViewFrom( main );
				if ( mainView != null && Cartographer.getUtil().getId( mainView ) == id ) {
					inHand = true;
					mainHand = true;
				}
			}
			
			if ( off != null ) {
				MapView offView = Cartographer.getUtil().getMapViewFrom( off );
				if ( offView != null && Cartographer.getUtil().getId( offView ) == id ) {
					inHand = true;
				}
			}
			
			if ( !inHand ) {
				new CartographerRendererDeactivateEvent( entry.getKey(), this ).callEvent();
				
				setting.deactivate();
				iterator.remove();
				continue;
			}
			
			Location location = player.getLocation();
			
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			Minimap map = getMinimap();
			boolean rotating = Cartographer.getInstance().isRotateByDefault();
			if ( map != null ) {
				if ( map.getSettings().getRotation() != BooleanOption.UNSET ) {
					rotating = map.getSettings().getRotation().isTrue();
				} else if ( viewer.getSetting( MapViewer.ROTATE ) != BooleanOption.UNSET ) {
					rotating = viewer.getSetting( MapViewer.ROTATE ).isTrue();
				}
			}
			
			if ( mainHand ) {
				double center = 180 - setting.getCursorYaw();
				double yaw = ( ( ( location.getYaw() + center ) % 360 ) + 360 ) % 360;
				// Deviation is how far off in degrees it is from the center
				double deviation = ( 180 - yaw );
				center = deviation * ( 128 / 40.0 );
				
				center = Math.min( 128, Math.max( -127, center ) );
				setting.setCursorX( -center );
				
				if ( deviation < -40 ) {
					deviation += 40;
				} else if ( deviation > 40 ) {
					deviation -= 40;
				} else {
					deviation = 0;
				}
				setting.cursorCenter -= deviation;
				
				// The pitch varies from 50 to 90
				double pitch = location.getPitch();
				
				pitch = Math.max( 50, Math.min( 90, pitch ) );
				pitch -= 50;
				pitch = pitch / 40.0;
				setting.setCursorY( ( pitch * 255 ) - 128 );
			}
			
			setting.rotating = rotating;
			setting.location = location;
			if ( setting.mainhand != mainHand ) {
				new CartographerRendererDeactivateEvent( entry.getKey(), this ).callEvent();
				setting.deactivate();
				
				setting.mainhand = mainHand;
				new CartographerRendererActivateEvent( player, this, mainHand ).callEvent();
			}
			setting.lastUpdated = System.currentTimeMillis();
		}
		
		if ( !ASYNC_RENDER ) {
			update();
		}
	}
	
	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
		id = Cartographer.getUtil().getId( view );

		ItemStack main = Cartographer.getUtil().getMainHandItem( player );
		ItemStack off = Cartographer.getUtil().getOffHandItem( player );
		
		// Only render if the map is in the player's hand. Otherwise, there's no point in updating.
		boolean inHand = false;
		boolean mainHand = false;
		if ( main != null ) {
			MapView mainView = Cartographer.getUtil().getMapViewFrom( main );
			if ( mainView != null && Cartographer.getUtil().getId( mainView ) == id ) {
				inHand = true;
				mainHand = true;
			}
		}
		
		if ( off != null ) {
			MapView offView = Cartographer.getUtil().getMapViewFrom( off );
			if ( offView != null && Cartographer.getUtil().getId( offView ) == id ) {
				inHand = true;
			}
		}
		
		// If the player isn't holding the map...
		if ( !inHand ) {
			PlayerSetting setting = settings.remove( player.getUniqueId() );
			if ( setting != null ) {
				// Deactivate the map if it's active
				new CartographerRendererDeactivateEvent( player.getUniqueId(), this ).callEvent();
				
				setting.deactivate();
			}
			return;
		}
		
		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		Minimap map = getMinimap();
		boolean rotating = Cartographer.getInstance().isRotateByDefault();
		double defaultZoom = 1;
		if ( map != null ) {
			defaultZoom = map.getSettings().getDefaultZoom();
			if ( map.getSettings().getRotation() != BooleanOption.UNSET ) {
				rotating = map.getSettings().getRotation().isTrue();
			} else if ( viewer.getSetting( MapViewer.ROTATE ) != BooleanOption.UNSET ) {
				rotating = viewer.getSetting( MapViewer.ROTATE ).isTrue();
			}
		}
		
		
		if ( !settings.containsKey( player.getUniqueId() ) ) {
			Location location = player.getLocation();
			PlayerSetting setting = new PlayerSetting( this, player.getUniqueId(), mapId, location );
			setting.rotating = rotating;
			setting.mainhand = mainHand;
			setting.lastUpdated = System.currentTimeMillis();
			setting.zoomscale = scales.getOrDefault( player.getUniqueId(), defaultZoom );
			settings.put( player.getUniqueId(), setting );
			
			if ( mainHand ) {
				// We know the minimap is in the player's main hand
				// The cursor for the player should be here too
				// Reset it
				setting.setCursorX( 0 );
				setting.setCursorY( 0 );
				setting.setCursorYaw( ( ( location.getYaw() % 360 ) + 360 ) % 360 );
			}
			new CartographerRendererActivateEvent( player, this, mainHand ).callEvent();
		} else if ( !TICK_RENDER ) {
			PlayerSetting setting = settings.get( player.getUniqueId() );
			setting.mainhand = mainHand;
			setting.location = player.getLocation();
			setting.rotating = rotating;
			setting.lastUpdated = System.currentTimeMillis();
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
}
