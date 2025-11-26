package io.github.bananapuncher714.cartographer.core.dependency;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;

public class DependencyManager {
	public DependencyManager( Plugin plugin ) {
	}
	
	public boolean shouldLocationBeLoaded( Location location ) {
		return false;
	}
	
	public boolean shouldChunkBeLoaded( ChunkLocation location ) {
		return false;
	}
	
	public String translateString( CommandSender sender, String string ) {
		return string;
	}
}
