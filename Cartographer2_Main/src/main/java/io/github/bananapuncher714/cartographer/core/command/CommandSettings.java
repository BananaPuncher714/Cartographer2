package io.github.bananapuncher714.cartographer.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;

public class CommandSettings implements CommandExecutor, TabCompleter {
	private Cartographer plugin;
	private SubCommand settingsCommand;
	
	public CommandSettings( Cartographer plugin ) {
		this.plugin = plugin;
		
		settingsCommand = new SubCommand( "settings" );
	}
	
	protected SubCommand getCommand() {
		return settingsCommand;
	}
	
	@Override
	public List< String > onTabComplete( CommandSender sender, Command arg1, String arg2, String[] args ) {
		List< String > aos = new ArrayList< String >();
		
		// TODO there must be a better way of doing this...
		// Perhaps like the new brigadier system...!?
		if ( args.length == 1 ) {
			if ( sender instanceof Player ) {
				if ( sender.hasPermission( "cartographer.settings.set" ) ) {
					aos.add( "set" );
				}
				if ( sender.hasPermission( "cartographer.settings.get" ) ) {
					aos.add( "get" );
				}
			}
			if ( sender.hasPermission( "cartographer.settings.setother" ) ) {
				aos.add( "setother" );
			}
			if ( sender.hasPermission( "cartographer.settings.getother" ) ) {
				aos.add( "getother" );
			}
		} else if ( args.length == 2 ) {
			if ( sender instanceof Player ) {
				if ( args[ 0 ].equalsIgnoreCase( "set" ) && sender.hasPermission( "cartographer.settings.set" ) ) {
					if ( sender.hasPermission( "cartographer.settings.set.rotate" ) ) {
						aos.add( "rotate" );
					}
					if ( sender.hasPermission( "cartographer.settings.set.cursor" ) ) {
						aos.add( "cursor" );
					}
				} else if ( args[ 0 ].equalsIgnoreCase( "get" ) && sender.hasPermission( "cartographer.settings.get" ) ) {
					if ( sender.hasPermission( "cartographer.settings.get.rotate" ) ) {
						aos.add( "rotate" );
					}
					if ( sender.hasPermission( "cartographer.settings.get.cursor" ) ) {
						aos.add( "cursor" );
					}
				}
			} else if ( ( args[ 0 ].equalsIgnoreCase( "setother" ) && sender.hasPermission( "cartographer.settings.setother" ) ) ||
					( args[ 0 ].equalsIgnoreCase( "getother" ) && sender.hasPermission( "cartographer.settings.getother" ) ) ) {
				for ( Player player : Bukkit.getOnlinePlayers() ) {
					aos.add( player.getName() );
				}
			}
		} else if ( args.length == 3 ) {
		}
		
		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], aos, completions );
		Collections.sort( completions );
		return completions;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command arg1, String arg2, String[] args ) {
		try {
			if ( args.length == 0 ) {
				sender.sendMessage( ChatColor.RED + "You must provide an argument!" );
			} else if ( args.length > 0 ) {
				String option = args[ 0 ];
				args = FailSafe.pop( args );
				if ( option.equalsIgnoreCase( "set" ) ) {
					sender.sendMessage( ChatColor.RED + "Invalid argument!" );
				}
			}
		} catch ( IllegalArgumentException exception ) {
			sender.sendMessage( exception.getMessage() );
		}
		return false;
	}

}
