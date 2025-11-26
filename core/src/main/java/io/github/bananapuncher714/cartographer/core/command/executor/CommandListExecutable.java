package io.github.bananapuncher714.cartographer.core.command.executor;

import java.util.Iterator;
import java.util.Set;

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
public class CommandListExecutable implements CommandExecutable {
	protected Cartographer plugin;
	
	public CommandListExecutable( Cartographer plugin ) {
		this.plugin = plugin;
	}

	@Override
	public void execute( CommandSender sender, String[] args, CommandParameters params ) {
		Set< String > minimaps = plugin.getMapManager().getMinimaps().keySet();
		if ( minimaps.isEmpty() ) {
			sender.sendMessage( ChatColor.AQUA + "There are currently no minimaps loaded!" );
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append( ChatColor.AQUA );
			builder.append( "Cartographer2 Minimaps (" );
			builder.append( minimaps.size() );
			builder.append( "): " );
			for ( Iterator< String > iterator = minimaps.iterator(); iterator.hasNext(); ) {
				String minimap = iterator.next();
				builder.append( ChatColor.YELLOW );
				builder.append( minimap );

				if ( iterator.hasNext() ) {
					builder.append( ChatColor.AQUA );
					builder.append( ", " );
				}
			}
			sender.sendMessage( builder.toString() );
		}
	}
}
