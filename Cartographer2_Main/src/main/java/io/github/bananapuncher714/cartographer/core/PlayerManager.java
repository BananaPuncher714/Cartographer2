package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import io.github.bananapuncher714.cartographer.core.api.events.player.MapViewerCreateEvent;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class PlayerManager {
	protected Cartographer plugin;
	protected File saveDirectory;
	
	protected Map< UUID, MapViewer > viewers = new HashMap< UUID, MapViewer >();
	
	public PlayerManager( Cartographer plugin, File saveDirectory ) {
		this.plugin = plugin;
		
		this.saveDirectory = saveDirectory;
	}
	
	/**
	 * Get the {@link MapViewer} for the specified UUID.
	 * 
	 * @param uuid
	 * The UUID of a player, cannot be null.
	 * @return
	 * The MapViewer associated with the player.
	 */
	public MapViewer getViewerFor( UUID uuid ) {
		Validate.notNull( uuid );
		MapViewer viewer = viewers.get( uuid );
		if ( viewer == null ) {
			viewer = load( uuid );
			new MapViewerCreateEvent( viewer ).callEvent();
			viewers.put( uuid, viewer );
		}
		return viewer;
	}
	
	public String getLocale( UUID uuid ) {
		return getViewerFor( uuid ).getSetting( MapViewer.LOCALE );
	}
	
	public void setLocale( UUID uuid, String locale ) {
		Bukkit.getScheduler().runTask( plugin, () -> {
			getViewerFor( uuid ).setSetting( MapViewer.LOCALE, locale );
		} );
	}
	
	protected MapViewer load( UUID uuid ) {
		File file = getFileFor( uuid );
		if ( file.exists() ) {
			FileConfiguration config = YamlConfiguration.loadConfiguration( file );
			return new MapViewer( uuid, config );
		}
		return new MapViewer( uuid );
	}
	
	public void unload( UUID uuid ) {
		MapViewer viewer = viewers.remove( uuid );
		if ( viewer != null ) {
			File file = getFileFor( uuid );
			file.getParentFile().mkdirs();
			file.delete();
			try {
				file.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			FileConfiguration config = YamlConfiguration.loadConfiguration( file );
	
			viewer.saveTo( config );
			
			try {
				config.save( file );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private File getFileFor( UUID uuid ) {
		String str = uuid.toString();
		return new File( saveDirectory + "/" + str.substring( 0, 2 ) + "/" + str );
	}
}
