package io.github.bananapuncher714.cartographer.core.command.executor;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * @deprecated
 * An unused example
 * 
 * @author BananaPuncher714
 */
public class CommandMapReloadExecutable implements CommandExecutable {
	protected Cartographer plugin;
	
	public CommandMapReloadExecutable( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public void execute( CommandSender sender, String[] args, CommandParameters params ) {
		Minimap map = params.getLast( Minimap.class );
		File saveDir = map.getDataFolder();

		plugin.getMapManager().unload( map );
		plugin.getMapManager().load( saveDir );

		sender.sendMessage( ChatColor.AQUA + "Reloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}

}
