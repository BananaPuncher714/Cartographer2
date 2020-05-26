package io.github.bananapuncher714.cartographer.core.command;

import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorSettingState;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorNotPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class CommandSettings {
	private Cartographer plugin;
	private SubCommand settingsCommand;
	
	public CommandSettings( Cartographer plugin ) {
		this.plugin = plugin;
		
		rebuildCommand();
	}
	
	public void rebuildCommand() {
		SubCommand setOther = new SubCommand( new InputValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
		
		SubCommand getOther = new SubCommand( new InputValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
		SubCommand set = new SubCommand( "set" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set" ) )
				.addSenderValidator( new SenderValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
		SubCommand get = new SubCommand( "get" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get" ) )
				.addSenderValidator( new SenderValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
		for ( SettingState< ? > state : MapViewer.getStates() ) {
			if ( !state.isPrivate() ) {
				String id = state.getId();
				setOther.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother." + id ) )
						.add( new SubCommand( new InputValidatorSettingState( state ) )
								.defaultTo( this::setOther ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (" + state.getValues().stream().collect( Collectors.joining( "/" ) ) + ")" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (" + state.getValues().stream().collect( Collectors.joining( "/" ) ) + ")" ) ) );
				
				getOther.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother." + id ) )
						.defaultTo( this::getOther ) );
				
				set.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set." + id ) )
						.add( new SubCommand( new InputValidatorSettingState( state ) )
								.defaultTo( this::set ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (" + state.getValues().stream().collect( Collectors.joining( "/" ) ) + ")" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (" + state.getValues().stream().collect( Collectors.joining( "/" ) ) + ")" ) ) );
				
				get.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get." + id ) )
						.defaultTo( this::get ) );
			}
		}
		
		settingsCommand = new SubCommand( "settings" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.settings" ) )
				.add( new SubCommand( "set" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother" ) )
						.addSenderValidator( new SenderValidatorNotPlayer() )
						.add( setOther )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That player is not online!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer settings set <player> <setting> <value>" ) ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother" ) )
						.addSenderValidator( new SenderValidatorNotPlayer() )
						.add( getOther )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That player is not online!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer settings getother <player> <setting>" ) ) )
				.add( set )
				.add( get )
				.add( new SubCommand( "setother" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( setOther )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That player is not online!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer settings setother <player> <setting> <value>" ) ) )
				.add( new SubCommand( "getother" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( getOther )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "That player is not online!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer settings getother <player> <setting>" ) ) )
				.add( new SubCommand( "help" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.help" ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer settings help" ) )
						.defaultTo( this::help ) )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid argument!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide an argument!" ) );
	}
	
	protected SubCommand getCommand() {
		return settingsCommand;
	}
	
	private < T extends Comparable< T > > void set( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		String property = parameters.get( String.class, 3 );
		String value = parameters.get( String.class, 4 );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			Optional< T > optional = state.getFrom( value );
			viewer.setSetting( state, optional.isPresent() ? optional.get() : state.getDefault() );
			sender.sendMessage( ChatColor.GREEN + "Set '" + state.getId() + "' to " + ChatColor.LIGHT_PURPLE + viewer.getSetting( state ) + ChatColor.GREEN + "." );
		}
	}
	
	private < T extends Comparable< T > > void get( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		String property = parameters.getLast( String.class );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			sender.sendMessage( "'" + state.getId() + "' is set to " + ChatColor.LIGHT_PURPLE + state.convertToString( viewer.getSetting( state ) ) + ChatColor.GREEN + "." );
		}
	}
	
	private < T extends Comparable< T > > void setOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.get( String.class, 4 );
		String value = parameters.get( String.class, 5 );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			Optional< T > optional = state.getFrom( value );
			viewer.setSetting( state, optional.isPresent() ? optional.get() : state.getDefault() );
			sender.sendMessage( ChatColor.GREEN + "Set '" + state.getId() + "' to " + ChatColor.LIGHT_PURPLE + viewer.getSetting( state ) + ChatColor.GREEN + " for " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + "." );
		}
	}

	private < T extends Comparable< T > > void getOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.getLast( String.class );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			sender.sendMessage( ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN +  " has '" + state.getId() + "' set to " + ChatColor.LIGHT_PURPLE + state.convertToString( viewer.getSetting( state ) ) + ChatColor.GREEN + "." );
		}
	}
	
	private void help( CommandSender sender, String[] args, CommandParameters parameters ) {
		sender.sendMessage( ChatColor.AQUA + "=== Cartographer Settings Commands ===" );
		sender.sendMessage( ChatColor.YELLOW + "/cartographer settings set <property> <value>" + ChatColor.GOLD + " - Set a property" );
		sender.sendMessage( ChatColor.YELLOW + "/cartographer settings get <property>" + ChatColor.GOLD + " - Get the value of a property" );
		sender.sendMessage( ChatColor.YELLOW + "/cartographer settings setother <player> <property> <value>" + ChatColor.GOLD + " - Set a property of a player" );
		sender.sendMessage( ChatColor.YELLOW + "/cartographer settings getother <player> <property>" + ChatColor.GOLD + " - Get the value of a property of a player" );
	}
}
