package io.github.bananapuncher714.cartographer.module.worldviewer.menu;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.map.DefaultPlayerCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;
import io.github.bananapuncher714.cartographer.core.map.process.DataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.renderer.FrameRenderTask;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.module.worldviewer.WorldViewer;

public class OverviewMenu extends MapMenu {
	private Cartographer plugin;
	private WorldViewer module;
	private double scale = -1;
	private Location center;
	
	public OverviewMenu( Cartographer plugin, WorldViewer module ) {
		canvas.setDither( false );
		this.plugin = plugin;
		this.module = module;
		scale = module.getDefaultScale();
	}
	
	public boolean view( Player player, PlayerSetting setting ) {
		viewers.add( player.getUniqueId() );
		canvas.getCursors().clear();
		canvas.clear();
		
		String mapId = setting.getMap();
		if ( mapId == null ) {
			return true;
		}
		
		Minimap map = plugin.getMapManager().getMinimaps().get( mapId );
		Location location = setting.getLocation();
		if ( center == null ) {
			center = location.clone();
		} else {
			MapViewer mViewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			double multiplier = mViewer.getSetting( module.getSetting() );
			int x = ( int ) Math.max( -128, Math.min( 127, setting.getCursorX() ) );
			int y = ( int ) Math.max( -128, Math.min( 127, setting.getCursorY() ) );

			if ( x == -128 ) {
				center.subtract( scale * multiplier, 0, 0 );
			} else if ( x == 127 ) {
				center.add( scale * multiplier, 0, 0 );
			}
			
			if ( y == -128 ) {
				center.subtract( 0, 0, scale * multiplier );
			} else if ( y == 127 ) {
				center.add( 0, 0, scale * multiplier );
			}
		}
		
		// Render the minimap
		List< FrameRenderTask > tasks = new ArrayList< FrameRenderTask >();
		
		DataCache cache = map.getDataCache();

		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		
		SimpleImage backgroundImage = plugin.getSettings().getBackground();
		if ( map.getBackgroundImage() != null ) {
			backgroundImage = map.getBackgroundImage();
		} else if ( viewer.getBackground() != null ) {
			backgroundImage = viewer.getBackground();
		}
		
		// Everything after this point can be done async
		CustomRenderInfo renderInfo = new CustomRenderInfo();
		
		CustomPlayerSetting customSetting = new CustomPlayerSetting( null, setting.getUUID(), mapId, center );
		customSetting.setScale( scale );
		customSetting.setRotation( false );
		
		renderInfo.setSetting( customSetting );
		renderInfo.setUUID( player.getUniqueId() );
		
		renderInfo.setMap( map );
		renderInfo.setCache( cache );

		if ( backgroundImage != null ) {
			renderInfo.setBackgroundImage( backgroundImage );
		}
		
		renderInfo.getWorldCursors().addAll( new DefaultPlayerCursorProvider().getCursors( player, map, setting ) );
		
		// Create a new task per player and run
		FrameRenderTask task = new CustomRenderTask( renderInfo );
		tasks.add( task );
		
		// Fork and join
		task.fork();
		task.join();
		
		byte[] display = renderInfo.getData();
		for ( int i = 0; i < display.length; i++ ) {
			int x = i % 128;
			int y = i / 128;
			canvas.setPixel( x, y, new Color( JetpImageUtil.getColorFromMinecraftPalette( display[ i ] ) ) );
		}
		
		for ( MapCursor cursor : renderInfo.getCursors() ) {
			canvas.getCursors().add( cursor );
		}
		
		for ( MenuComponent component : components ) {
			if ( component.onView( canvas, player, setting.getCursorX() / 2.0 + 64, setting.getCursorY() / 2.0 + 64 ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean interact( Player player, PlayerSetting setting ) {
		for ( MenuComponent component : components ) {
			if ( component.onInteract( player, setting.getCursorX() / 2.0 + 64, setting.getCursorY() / 2.0 + 64, setting.getInteraction() ) ) {
				return true;
			}
		}
		if ( setting.getInteraction() == MapInteraction.Q ) {
			return true;
		} else if ( setting.getInteraction() == MapInteraction.LEFT ) {
			scale = getLowerScale( module.getScales(), scale );
		} else if ( setting.getInteraction() == MapInteraction.RIGHT ) {
			scale = getHigherScale( module.getScales(), scale );
		}
		
		return false;
	}
	
	public double getScale() {
		return scale;
	}
	
	public Location getCenter() {
		return center;
	}
	
	private static double getHigherScale( double[] scales, double old ) {
		for ( int i = 0; i < scales.length; i++ ) {
			if ( scales[ i ] > old ) {
				return scales[ i ];
			}
		}
		return old;
	}
	
	private static double getLowerScale( double[] scales, double old ) {
		for ( int i = scales.length - 1; i >= 0; i-- ) {
			if ( scales[ i ] < old ) {
				return scales[ i ];
			}
		}
		return old;
	}
}
