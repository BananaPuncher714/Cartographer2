package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorInt;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorCreateMinimap;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorMinimap;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorMinimapFile;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Base Cartographer command.
 * 
 * @author BananaPuncher714
 */
public class CommandCartographer implements CommandExecutor, TabCompleter {
	private Cartographer plugin;
	private CommandModule moduleCommand;
	private CommandSettings settingsCommand;

	private PluginCommand command;
	private SubCommand subCommand;

	public CommandCartographer( Cartographer plugin, PluginCommand command ) {
		this.plugin = plugin;

		moduleCommand = new CommandModule( plugin );
		settingsCommand = new CommandSettings( plugin );

		this.command = command;
		subCommand = new SubCommand( "cartographer" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer" ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.reload" ) )
						.defaultTo( this::reload ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.reload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::reloadMap ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer reload <map>" ) ) )
				.add( new SubCommand( "list" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.list" ) )
						.defaultTo( this::list ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.get" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::get ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a minimap!" ) ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.give" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.add( new SubCommand( new InputValidatorPlayer() )
										.add( new SubCommand( new InputValidatorInt( 0, 40 ) )
												.defaultTo( this::give ) )
										.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid slot! (0-40)" ) )
										.defaultTo( this::give ) )
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That player is not online!" ) )
								.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a player!!" ) ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer get <map> <player> [slot]" ) ) )
				.add( new SubCommand( "create" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.create" ) )
						.add( new SubCommand( new InputValidatorCreateMinimap( plugin ) )
								.defaultTo( this::create ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "A minimap with that id already exists!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer create <id>" ) ) )
				.add( new SubCommand( "delete" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.delete" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::delete ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer delete <id>" ) ) )
				.add( new SubCommand( "load" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.load" ) )
						.add( new SubCommand( new InputValidatorMinimapFile( plugin ) )
								.defaultTo( this::load ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That minimap does not exist!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer delete <id>" ) ) )
				.add( new SubCommand( "unload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.unload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::unload ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer unload <id>" ) ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Invalid arguments!" ) )
				.apply( command );
	}

	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		List< String > aos = new ArrayList< String >();
		
		aos.addAll( subCommand.getTabCompletions( sender, args ) );

		/*
		if ( args.length > 1 && args[ 0 ].equalsIgnoreCase( "module" ) ) {
			// Module sub command
			String[] subArgs = FailSafe.pop( args );
			aos.addAll( moduleCommand.onTabComplete( sender, command, label, subArgs ) );
		} else if ( args.length > 1 && args[ 0 ].equalsIgnoreCase( "settings" ) ) {
			String[] subArgs = FailSafe.pop( args );
			aos.addAll( settingsCommand.onTabComplete( sender, command, label, subArgs ) );
		} else if ( args.length == 1 ) {
			if ( sender.hasPermission( "cartographer.reload" ) || sender.hasPermission( "cartographer.map.reload" ) ) {
				aos.add( "reload" );
			}
			if ( sender.hasPermission( "cartographer.map.get" ) || sender.hasPermission( "cartographer.map.give" ) ) {
				aos.add( "get" );
			}
			if ( sender.hasPermission( "cartographer.map.create" ) ) {
				aos.add( "create" );
			}
			if ( sender.hasPermission( "cartographer.map.delete" ) ) {
				aos.add( "delete" );
			}
			if ( sender.hasPermission( "cartographer.map.unload" ) ) {
				aos.add( "unload" );
			}
			if ( sender.hasPermission( "cartographer.map.load" ) ) {
				aos.add( "load" );
			}
			if ( sender.hasPermission( "cartographer.map.list" ) ) {
				aos.add( "list" );
			}
			if ( sender.hasPermission( "cartographer.module" ) ) {
				aos.add( "module" );
			}
			if ( sender.hasPermission( "cartographer.settings" ) ) {
				aos.add( "settings" );
			}
		} else if ( args.length == 2 ) {
			if ( ( args[ 0 ].equalsIgnoreCase( "get" ) && ( sender.hasPermission( "cartographer.map.get" ) || sender.hasPermission( "cartographer.map.give" ) ) ) ||
					( args[ 0 ].equalsIgnoreCase( "delete" ) && sender.hasPermission( "cartographer.map.delete" ) ) ||
					( args[ 0 ].equalsIgnoreCase( "unload" ) && sender.hasPermission( "cartographer.map.unload" ) ) || 
					( args[ 0 ].equalsIgnoreCase( "reload" ) && ( sender.hasPermission( "cartographer.reload" ) || sender.hasPermission( "cartographer.map.reload" ) ) ) ) {
				aos.addAll( plugin.getMapManager().getMinimaps().keySet() );
			} else if ( args[ 0 ].equalsIgnoreCase( "load" ) && sender.hasPermission( "cartographer.map.load" ) ) {
				for ( File file : Cartographer.getMapSaveDir().listFiles() ) {
					if ( !file.isDirectory() ) {
						continue;
					}

					String name = file.getName();
					if ( !plugin.getMapManager().getMinimaps().containsKey( name ) ) {
						aos.add( name );
					}
				}
			}
		} else if ( args.length == 3 ) {
			if ( args[ 0 ].equalsIgnoreCase( "get" ) && sender.hasPermission( "cartographer.map.give" ) ) {
				for ( Player player : Bukkit.getOnlinePlayers() ) {
					aos.add( player.getName() );
				}
			}
		}
		*/

		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], aos, completions );
		Collections.sort( completions );
		return completions;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		CommandParameters parameters = new CommandParameters();
		subCommand.submit( sender, command.getName(), args, parameters ).execute( sender );
		return false;
	}

	private void list( CommandSender sender, String[] args, CommandParameters parameters ) {
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

	private void create( CommandSender sender, String[] args, CommandParameters parameters ) {
		Validate.isTrue( sender.hasPermission( "cartographer.map.create" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer create <id>" );

		Minimap map = plugin.getMapManager().constructNewMinimap( args[ 0 ] );
		sender.sendMessage( ChatColor.GREEN + "Created and registered a new minimap with id '" + ChatColor.YELLOW + args[ 0 ] + ChatColor.GREEN + "'" );
	}

	private void get( CommandSender sender, String[] args, CommandParameters parameters ) {
		Minimap map = parameters.getLast( Minimap.class );
		Player player = ( Player ) sender;
		player.getInventory().addItem( plugin.getMapManager().getItemFor( map ) );
	}
	
	private void give( CommandSender sender, String[] args, CommandParameters parameters ) {
		// /cartographer get <map> <player> [slot]
		Minimap map = parameters.getLast( Minimap.class );
		Player player = parameters.getLast( Player.class );
		int slot = -1;
		if ( parameters.size() > 4 ) {
			slot = parameters.getLast( int.class );
		}

		if ( slot == -1 ) {
			player.getInventory().addItem( plugin.getMapManager().getItemFor( map ) );
		} else {
			player.getInventory().setItem( slot, plugin.getMapManager().getItemFor( map ) );
		}
	}

	private void delete( CommandSender sender, String[] args, CommandParameters parameters ) {
		Minimap map = parameters.getLast( Minimap.class );
		plugin.getMapManager().remove( map );
		sender.sendMessage( ChatColor.AQUA + "Deleted minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}
	
	private void reload( CommandSender sender, String[] args, CommandParameters parameters ) {
		// Reload the Cartographer config and whatnot
		plugin.reload();
		sender.sendMessage( ChatColor.AQUA + "Reloaded Cartographer2 settings");
	}

	private void reloadMap( CommandSender sender, String[] args, CommandParameters parameters ) {
		// Reload the minimap
		// Essentially unloading it then loading it again
		Minimap map = parameters.getLast( Minimap.class );
		File saveDir = map.getDataFolder();

		plugin.getMapManager().unload( map );
		plugin.getMapManager().load( saveDir );

		sender.sendMessage( ChatColor.AQUA + "Reloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}

	private void unload( CommandSender sender, String[] args, CommandParameters parameters ) {
		Minimap map = parameters.getLast( Minimap.class );
		plugin.getMapManager().unload( map );
		sender.sendMessage( ChatColor.AQUA + "Unloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}

	private void load( CommandSender sender, String[] args, CommandParameters parameters ) {
		File mapDir = parameters.getLast( File.class );

		Minimap map = plugin.getMapManager().load( mapDir );

		sender.sendMessage( ChatColor.AQUA + "Loaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}
}
