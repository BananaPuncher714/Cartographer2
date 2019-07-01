package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.process.SimpleChunkProcessor;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class MinimapManager {
	protected Cartographer plugin;
	protected Map< String, Minimap > minimaps = new ConcurrentHashMap< String, Minimap >();
	protected Map< UUID, Minimap > currentMap = new ConcurrentHashMap< UUID, Minimap >();
	protected Minimap defaultMinimap;
	
	public MinimapManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	public Map< String, Minimap > getMinimaps() {
		return minimaps;
	}
	
	public void convert( MapView view, Minimap map ) {
		for ( MapRenderer render : view.getRenderers() ) {
			view.removeRenderer( render );
		}
		CartographerRenderer renderer = new CartographerRenderer( map );
		plugin.getRenderers().put( view.getId(), renderer );
		view.addRenderer( renderer );
		plugin.getHandler().registerMap( view.getId() );
	}
	
	protected void update() {
		for ( Minimap map : minimaps.values() ) {
			map.update();
		}
	}
	
	public void registerMinimap( Minimap minimap ) {
		minimaps.put( minimap.getId(), minimap );
		if ( defaultMinimap == null ) {
			defaultMinimap = minimap;
		}
	}
	
	public Minimap getCurrentMap( UUID uuid ) {
		Minimap map = currentMap.get( uuid );
		if ( map == null ) {
			return defaultMinimap;
		}
		return map;
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
