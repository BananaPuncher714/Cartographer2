package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapDeleteEvent;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapUnloadEvent;
import io.github.bananapuncher714.cartographer.core.api.events.renderer.CartographerRendererCreateEvent;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.SimpleChunkProcessor;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.nbteditor.NBTEditor;

public class MinimapManager {
	private static final Object[] MAP_ID = { "io", "github", "bananapuncher714", "cartographer", "map-id" };
	
	protected Cartographer plugin;
	protected Map< String, Minimap > minimaps = new ConcurrentHashMap< String, Minimap >();

	protected CartographerLogger logger = new CartographerLogger( "MapManager" );
	
	public MinimapManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	public Map< String, Minimap > getMinimaps() {
		return minimaps;
	}
	
	public boolean isMinimapItem( ItemStack item ) {
		return NBTEditor.getString( item, MAP_ID ) != null;
	}
	
	public ItemStack getItemFor( Minimap map ) {
		MapView view = Bukkit.createMap( Bukkit.getWorlds().get( 0 ) );
		while ( Cartographer.getInstance().getInvalidIds().contains( getId( view ) ) ) {
			view = Bukkit.createMap( Bukkit.getWorlds().get( 0 ) );
		}
		
		ItemStack mapItem = plugin.getHandler().getUtil().getMapItem( getId( view ) );
		
		convert( view, map );
		
		String id = map == null ? "MISSING MAP" : map.getId();
		mapItem = NBTEditor.set( mapItem, id, MAP_ID );
		
		return mapItem;
	}
	
	public ItemStack getItemFor( Minimap map, int mapId ) {
		MapView view = plugin.getHandler().getUtil().getMap( mapId );

		ItemStack mapItem = plugin.getHandler().getUtil().getMapItem( mapId );
		
		convert( view, map );
		
		String id = map == null ? "MISSING MAP" : map.getId();
		mapItem = NBTEditor.set( mapItem, id, MAP_ID );
		
		return mapItem;
	}
	
	/**
	 * Get the {@link CartographerRenderer} of a MapView.
	 * 
	 * @param view
	 * The MapView to get it from.
	 * @return
	 * Null if the MapView provided is null, or does not contain a {@link CartographerRenderer}.
	 */
	public CartographerRenderer getRendererFrom( MapView view ) {
		if ( view == null ) {
			return null;
		}
		for ( MapRenderer renderer : view.getRenderers() ) {
			if ( renderer instanceof CartographerRenderer ) {
				return ( CartographerRenderer ) renderer;
			}
		}
		return null;
	}
	
	public void update( ItemStack item ) {
		MapView view = plugin.getHandler().getUtil().getMapViewFrom( item );
		
		String mapId = NBTEditor.getString( item, MAP_ID );
		if ( mapId != null ) {
			Minimap map = minimaps.get( mapId );
			convert( view, map );
		}
	}
	
	public ItemStack update( ItemStack item, Minimap newMap ) {
		MapView view = plugin.getHandler().getUtil().getMapViewFrom( item );
		
		convert( view, newMap );
		
		item = NBTEditor.set( item, newMap.getId(), MAP_ID );
		
		return item;
	}
	
	public void activate( Player player, MapInteraction interaction ) {
		ItemStack item = Cartographer.getUtil().getMainHandItem( player );
		CartographerRenderer renderer = getRendererFrom( Cartographer.getUtil().getMapViewFrom( item ) );
		if ( renderer != null ) {
			Bukkit.getScheduler().runTask( plugin, () -> {
				renderer.interact( player, interaction );
			} );
		}
	}
	
	/**
	 * Change the given MapView to a Cartographer2 {@link Minimap}.
	 * 
	 * @param view
	 * @param map
	 */
	public void convert( MapView view, Minimap map ) {
		boolean converted = false;
		for ( MapRenderer render : view.getRenderers() ) {
			if ( render instanceof CartographerRenderer ) {
				CartographerRenderer renderer = ( CartographerRenderer ) render;
				renderer.setMinimap( map );
				converted = true;
			} else {
				view.removeRenderer( render );
			}
		}
		if ( !converted ) {
			CartographerRenderer renderer = new CartographerRenderer( plugin, map );
			plugin.getRenderers().put( getId( view ), renderer );
			view.addRenderer( renderer );
			plugin.getHandler().registerMap( getId( view ) );
			
			new CartographerRendererCreateEvent( renderer ).callEvent();
		}
	}
	
	protected void update() {
		for ( Minimap map : minimaps.values() ) {
			map.update();
		}
	}
	
	public void registerMinimap( Minimap minimap ) {
		minimaps.put( minimap.getId(), minimap );
	}
	
	public Minimap constructNewMinimap( String id ) {
		logger.infoTr( LocaleConstants.MANAGER_MINIMAP_LOADING, id );
		File dir = plugin.getAndConstructMapDir( id );
		File config = new File( dir + "/" + "config.yml" );
		MapSettings settings = new MapSettings( YamlConfiguration.loadConfiguration( config ) );
		
		MapDataCache cache = new MapDataCache( settings );
		cache.setChunkDataProvider( new SimpleChunkProcessor( cache, settings.getPalette() ) );
		
		Minimap map = new Minimap( id, settings.getPalette(), cache, dir, settings );
		registerMinimap( map );

		new MinimapLoadEvent( map ).callEvent();
		
		return map;
	}
	
	public Minimap load( File dir ) {
		return constructNewMinimap( dir );
	}
	
	public Minimap constructNewMinimap( File dir ) {
		logger.infoTr( LocaleConstants.MANAGER_MINIMAP_LOADING, dir.getName() );
		plugin.saveMapFiles( dir );
		File config = new File( dir + "/" + "config.yml" );
		MapSettings settings = new MapSettings( YamlConfiguration.loadConfiguration( config ) );
		
		MapDataCache cache = new MapDataCache( settings );
		cache.setChunkDataProvider( new SimpleChunkProcessor( cache, settings.getPalette() ) );
		
		Minimap map = new Minimap( dir.getName(), settings.getPalette(), cache, dir, settings );
		registerMinimap( map );
		
		new MinimapLoadEvent( map ).callEvent();
		
		return map;
	}
	
	public void unload( Minimap map ) {
		logger.infoTr( LocaleConstants.MANAGER_MINIMAP_UNLOADING, map.getId() );
		new MinimapUnloadEvent( map ).callEvent();
		
		minimaps.remove( map.getId() );
		if ( map != null ) {
			map.terminate();
		}
	}
	
	public void remove( Minimap map ) {
		logger.infoTr( LocaleConstants.MANAGER_MINIMAP_DELETING, map.getId() );
		new MinimapDeleteEvent( map ).callEvent();
		
		minimaps.remove( map.getId() );
		if ( map != null ) {
			map.terminate();
		}
		
		File file = plugin.getMapDirFor( map.getId() );
		FileUtil.recursiveDelete( file );
	}
	
	protected void terminate() {
		for ( Minimap map : minimaps.values() ) {
			map.terminate();
		}
	}
	
	private int getId( MapView view ) {
		return Cartographer.getUtil().getId( view );
	}
}
