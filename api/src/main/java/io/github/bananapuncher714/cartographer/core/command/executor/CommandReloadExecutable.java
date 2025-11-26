package io.github.bananapuncher714.cartographer.core.command.executor;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable;

/**
 * @deprecated
 * An unused example
 * 
 * @author BananaPuncher714
 */
public class CommandReloadExecutable implements CommandExecutable {
	protected Cartographer plugin;
	
	public CommandReloadExecutable( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public void execute( CommandSender sender, String[] args, CommandParameters params ) {
		plugin.reload();
		sender.sendMessage( ChatColor.AQUA + "Reloaded Cartographer2 settings");
	}
}
