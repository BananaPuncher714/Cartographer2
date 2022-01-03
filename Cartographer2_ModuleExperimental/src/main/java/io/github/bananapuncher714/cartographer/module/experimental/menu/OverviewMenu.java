package io.github.bananapuncher714.cartographer.module.experimental.menu;

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

public class OverviewMenu extends MapMenu {
	private Cartographer plugin;
	private double scale = -1;
	private byte[] display = new byte[ 128 * 128 ];
	private Location center;
	
	public OverviewMenu( Cartographer plugin ) {
		canvas.setDither( false );
		this.plugin = plugin;
	}
	
	public boolean view( Player player, PlayerSetting setting ) {
		viewers.add( player.getUniqueId() );
		canvas.getCursors().clear();
		for ( MenuComponent component : components ) {
			if ( component.onView( canvas, player, setting.getCursorX() / 2.0 + 64, setting.getCursorY() / 2.0 + 64 ) ) {
				return true;
			}
		}
		
		String mapId = setting.getMap();
		if ( mapId == null ) {
			return true;
		}
		
		Minimap map = plugin.getMapManager().getMinimaps().get( mapId );
		Location location = setting.getLocation();
		if ( scale == -1 ) {
			scale = setting.getScale();
			center = location.clone();
		} else {
			int x = ( int ) Math.max( -128, Math.min( 127, setting.getCursorX() ) );
			int y = ( int ) Math.max( -128, Math.min( 127, setting.getCursorY() ) );

			if ( x == -128 ) {
				center.subtract( scale * 2, 0, 0 );
			} else if ( x == 127 ) {
				center.add( scale * 2, 0, 0 );
			}
			
			if ( y == -128 ) {
				center.subtract( 0, 0, scale * 2 );
			} else if ( y == 127 ) {
				center.add( 0, 0, scale * 2 );
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
		
		display = renderInfo.getData();
		for ( MapCursor cursor : renderInfo.getCursors() ) {
			canvas.getCursors().add( cursor );
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
			scale /= 2;
		} else if ( setting.getInteraction() == MapInteraction.RIGHT ) {
			scale *= 2;
		}
		
		return false;
	}
	
	@Override
	public byte[] getDisplay() {
		return display;
	}
}
