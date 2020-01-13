package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

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
public class CommandCartographer {
	private Cartographer plugin;
	private CommandSettings settingsCommand;
	private CommandModule moduleCommand;

	private PluginCommand command;
	private SubCommand mainCommand;

	public CommandCartographer( Cartographer plugin, PluginCommand command ) {
		this.plugin = plugin;

		moduleCommand = new CommandModule( plugin );
		settingsCommand = new CommandSettings( plugin );
		
		this.command = command;
		mainCommand = new SubCommand( "cartographer" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer" ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.reload" ) )
						.defaultTo( this::reload ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.reload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer reload <map>" ) )
								.defaultTo( this::reloadMap ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer reload <map>" ) ) )
				.add( new SubCommand( "list" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.list" ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer list" ) )
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
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer create <id>" ) )
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
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer load <id>" ) ) )
				.add( new SubCommand( "unload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.unload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::unload ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid minimap!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer unload <id>" ) ) )
				.add( moduleCommand.getCommand() )
				.add( settingsCommand.getCommand() )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid argument!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide an argument!" ) )
				.applyTo( command );
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
		String name = parameters.getLast( String.class );
		Minimap map = plugin.getMapManager().constructNewMinimap( name );
		sender.sendMessage( ChatColor.AQUA + "Created and registered a new minimap with id '" + ChatColor.YELLOW + name + ChatColor.AQUA + "'" );
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
