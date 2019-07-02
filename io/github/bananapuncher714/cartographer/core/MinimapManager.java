package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.SimpleChunkProcessor;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.NBTEditor;

public class MinimapManager {
	private static final Object[] MAP_ID = { "io", "github", "bananapuncher714", "cartographer", "map-id" };
	
	protected Cartographer plugin;
	protected Map< String, Minimap > minimaps = new ConcurrentHashMap< String, Minimap >();
	
	public MinimapManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	public Map< String, Minimap > getMinimaps() {
		return minimaps;
	}
	
	public ItemStack getItemFor( Minimap map ) {
		MapView view = Bukkit.createMap( Bukkit.getWorlds().get( 0 ) );
		while ( Cartographer.getInstance().getInvalidIds().contains( view.getId() ) ) {
			view = Bukkit.createMap( Bukkit.getWorlds().get( 0 ) );
		}
		
		ItemStack mapItem = plugin.getHandler().getUtil().getMapItem( view.getId() );
		
		convert( view, map );
		
		mapItem = NBTEditor.set( mapItem, map.getId(), MAP_ID );
		
		return mapItem;
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
			CartographerRenderer renderer = new CartographerRenderer( map );
			plugin.getRenderers().put( view.getId(), renderer );
			view.addRenderer( renderer );
			plugin.getHandler().registerMap( view.getId() );
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
		File dir = plugin.getAndConstructMapDir( id );
		File config = new File( dir + "/" + "config.yml" );
		MapSettings settings = new MapSettings( YamlConfiguration.loadConfiguration( config ) );
		
		MapDataCache cache = new MapDataCache();
		cache.setChunkDataProvider( new SimpleChunkProcessor( cache, settings.getPalette() ) );
		
		Minimap map = new Minimap( id, settings.getPalette(), cache, dir, settings );
		registerMinimap( map );
		return map;
	}
	
	protected void terminate() {
		for ( Minimap map : minimaps.values() ) {
			map.terminate();
		}
	}
}
