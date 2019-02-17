package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bananapuncher714.cartographer.core.map.ChunkDataProvider;
import io.github.bananapuncher714.cartographer.core.map.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.SimpleChunkProcessor;

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
		MapDataCache cache = new MapDataCache();
		cache.setChunkDataProvider( new SimpleChunkProcessor( cache, plugin.getPalette() ) );
		Minimap map = new Minimap( id, plugin.getPalette(), cache, new File( plugin.getDataFolder() + "/maps/" + id ), new MapSettings() );
		registerMinimap( map );
		return map;
	}
	
	protected void terminate() {
		for ( Minimap map : minimaps.values() ) {
			map.terminate();
		}
	}
}
