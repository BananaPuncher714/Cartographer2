package io.github.bananapuncher714.cartographer.core.dependency;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class DependencyManager {
	protected Plugin plugin;
	
	protected DependencyWorldBorderAPI dependencyWorldBorder;
	protected DependencyVanilla dependencyVanilla;
	protected DependencyPlaceholderAPI dependencyPlaceholder;
	
	public DependencyManager( Plugin plugin ) {
		this.plugin = plugin;
		
		Plugin worldBorderAPI = Bukkit.getPluginManager().getPlugin( "WorldBorder" );
		if ( worldBorderAPI != null && worldBorderAPI.isEnabled() ) {
			dependencyWorldBorder = new DependencyWorldBorderAPI();
		}
		
		Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin( "PlaceholderAPI" ); 
		if ( placeholderAPI != null ) {
			dependencyPlaceholder = new DependencyPlaceholderAPI();
		}
		
		dependencyVanilla = new DependencyVanilla();
	}
	
	public boolean shouldLocationBeLoaded( Location location ) {
		boolean loaded = false;
		
		if ( dependencyWorldBorder != null ) {
			loaded = dependencyWorldBorder.shouldLocationBeLoaded( location );
		}
		
		if ( !loaded ) {
			loaded = dependencyVanilla.shouldLocationBeLoaded( location );
		}
		
		return loaded;
	}
	
	public boolean shouldChunkBeLoaded( ChunkLocation location ) {
		boolean loaded = false;

		Location ne = new Location( location.getWorld(), location.getX() << 4 | 0xF, 0, location.getZ() << 4 );
		Location nw = new Location( location.getWorld(), location.getX() << 4, 0, location.getZ() << 4 );
		Location se = new Location( location.getWorld(), location.getX() << 4 | 0xF, 0, location.getZ() << 4 | 0xF );
		Location sw = new Location( location.getWorld(), location.getX() << 4, 0, location.getZ() << 4 | 0xF );
		
		if ( dependencyWorldBorder != null ) {
			loaded = dependencyWorldBorder.shouldLocationBeLoaded( ne ) ||
					dependencyWorldBorder.shouldLocationBeLoaded( nw ) ||
					dependencyWorldBorder.shouldLocationBeLoaded( se ) ||
					dependencyWorldBorder.shouldLocationBeLoaded( sw );
		}
		
		if ( !loaded ) {
			loaded = dependencyVanilla.shouldLocationBeLoaded( ne ) ||
					dependencyVanilla.shouldLocationBeLoaded( nw ) ||
					dependencyVanilla.shouldLocationBeLoaded( se ) ||
					dependencyVanilla.shouldLocationBeLoaded( sw );
		}
		
		return loaded;
	}
	
	public String translateString( CommandSender sender, String string ) {
		if ( dependencyPlaceholder != null ) {
			if ( string != null ) {
				return dependencyPlaceholder.translate( sender, string );
			}
		}
		return string;
	}
}
