package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessageLocale;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorInt;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorCreateMinimap;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorMinimap;
import io.github.bananapuncher714.cartographer.core.command.validator.InputValidatorMinimapFile;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
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
		rebuildCommand();
	}
	
	public CommandSettings getSettingsCommand() {
		return settingsCommand;
	}
	
	public CommandModule getModuleCommand() {
		return moduleCommand;
	}

	public void rebuildCommand() {
		settingsCommand.rebuildCommand();
		
		mainCommand = new SubCommand( "cartographer" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer" ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.reload" ) )
						.defaultTo( this::reload ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.reload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_RELOAD_USAGE ) )
								.defaultTo( this::reloadMap ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_RELOAD_USAGE ) ) )
				.add( new SubCommand( "list" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.list" ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_LIST_USAGE ) )
						.defaultTo( this::list ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.get" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::get ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_MINIMAP ) ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.give" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.add( new SubCommand( new InputValidatorPlayer() )
										.add( new SubCommand( new InputValidatorInt( 0, 40 ) )
												.defaultTo( this::give ) )
										.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_SLOT ) )
										.defaultTo( this::give ) )
								.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PLAYER_MISSING ) )
								.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_PLAYER) ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_GET_USAGE ) ) )
				.add( new SubCommand( "create" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.create" ) )
						.add( new SubCommand( new InputValidatorCreateMinimap( plugin ) )
								.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_CREATE_USAGE ) )
								.defaultTo( this::create ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_MINIMAP_EXISTS ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_CREATE_USAGE ) ) )
				.add( new SubCommand( "delete" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.delete" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::delete ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_DELETE_USAGE ) ) )
				.add( new SubCommand( "load" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.load" ) )
						.add( new SubCommand( new InputValidatorMinimapFile( plugin ) )
								.defaultTo( this::load ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_LOAD_USAGE ) ) )
				.add( new SubCommand( "unload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.map.unload" ) )
						.add( new SubCommand( new InputValidatorMinimap( plugin ) )
								.defaultTo( this::unload ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_MINIMAP ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_UNLOAD_USAGE ) ) )
				.add( new SubCommand( "help" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.help" ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_CARTOGRAPHER_HELP_USAGE ) )
						.defaultTo( this::help ) )
				.add( moduleCommand.getCommand() )
				.add( settingsCommand.getCommand() )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_ARGUMENT ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_ARGUMENT ) )
				.applyTo( command );
	}
	
	private void list( CommandSender sender, String[] args, CommandParameters parameters ) {
		Set< String > minimaps = plugin.getMapManager().getMinimaps().keySet();
		if ( minimaps.isEmpty() ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_LIST_EMPTY );
		} else {
			StringBuilder builder = new StringBuilder();
			for ( Iterator< String > iterator = minimaps.iterator(); iterator.hasNext(); ) {
				String minimap = iterator.next();
				builder.append( ChatColor.YELLOW );
				builder.append( minimap );

				if ( iterator.hasNext() ) {
					builder.append( ChatColor.AQUA );
					builder.append( ", " );
				}
			}
			
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_LIST_FORMAT, minimaps.size(), builder.toString() );
		}
	}

	private void create( CommandSender sender, String[] args, CommandParameters parameters ) {
		String name = parameters.getLast( String.class );
		Minimap map = plugin.getMapManager().constructNewMinimap( name );
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_CREATE_SUCCESS, map.getId() );
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
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_DELETE_SUCCESS, map.getId() );
	}
	
	private void reload( CommandSender sender, String[] args, CommandParameters parameters ) {
		// Reload the Cartographer config and whatnot
		plugin.reload();
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_RELOAD_PLUGIN );
	}

	private void reloadMap( CommandSender sender, String[] args, CommandParameters parameters ) {
		// Reload the minimap
		// Essentially unloading it then loading it again
		Minimap map = parameters.getLast( Minimap.class );
		File saveDir = map.getDataFolder();

		plugin.getMapManager().unload( map );
		plugin.getMapManager().load( saveDir );
		
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_RELOAD_MINIMAP, map.getId() );
	}

	private void unload( CommandSender sender, String[] args, CommandParameters parameters ) {
		Minimap map = parameters.getLast( Minimap.class );
		plugin.getMapManager().unload( map );
		
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_UNLOAD_SUCCESS, map.getId() );
	}

	private void load( CommandSender sender, String[] args, CommandParameters parameters ) {
		File mapDir = parameters.getLast( File.class );

		Minimap map = plugin.getMapManager().load( mapDir );

		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_CARTOGRAPHER_LOAD_SUCCESS, map.getId() );
	}
	
	private void help( CommandSender sender, String[] args, CommandParameters parameters ) {
		for ( int i = 0; i < 20; i++ ) {
			plugin.getLocaleManager().translateAndSend( sender, String.format( LocaleConstants.COMMAND_CARTOGRAPHER_HELP_FORMAT, i ) );
		}
	}
}
