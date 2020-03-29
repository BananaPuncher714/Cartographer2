package io.github.bananapuncher714.cartographer.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorBoolean;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorBooleanOption;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorNotPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class CommandSettings {
	private Cartographer plugin;
	private SubCommand settingsCommand;
	
	public CommandSettings( Cartographer plugin ) {
		this.plugin = plugin;
		
		SubCommand setOther = new SubCommand( new InputValidatorPlayer() )
				.add( new SubCommand( "showname" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother.showname" ) )
						.add( new SubCommand( new InputValidatorBoolean( new String[] { "on", "true" }, new String[] { "off", "false" } ) )
								.defaultTo( this::setOther ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/off/false)" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/off/false)" ) ) )
				.add( new SubCommand( "cursor" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother.cursor" ) )
						.add( new SubCommand( new InputValidatorBoolean( new String[] { "on", "true" }, new String[] { "off", "false" } ) )
								.defaultTo( this::setOther ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/off/false)" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/off/false)" ) ) )
				.add( new SubCommand( "rotate" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother.rotate" ) )
						.add( new SubCommand( new InputValidatorBooleanOption( new String[] { "on", "true" }, new String[] { "off", "false" }, new String[] { "unset" } ) )
								.defaultTo( this::setOther ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/unset/off/false)" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/unset/off/false)" ) ) )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
		SubCommand getOther = new SubCommand( new InputValidatorPlayer() )
				.add( new SubCommand( "showname" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother.showname" ) )
						.defaultTo( this::getOther ) )
				.add( new SubCommand( "cursor" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother.cursor" ) )
						.defaultTo( this::getOther ) )
				.add( new SubCommand( "rotate" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother.rotate" ) )
						.defaultTo( this::getOther ) )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) );
		
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
				.add( new SubCommand( "set" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( new SubCommand( "showname" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set.showname" ) )
								.add( new SubCommand( new InputValidatorBoolean( new String[] { "on", "true" }, new String[] { "off", "false" } ) )
										.defaultTo( this::set ) )
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/off/false)" ) )
								.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/off/false)" ) ) )
						.add( new SubCommand( "cursor" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set.cursor" ) )
								.add( new SubCommand( new InputValidatorBoolean( new String[] { "on", "true" }, new String[] { "off", "false" } ) )
										.defaultTo( this::set ) )
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/off/false)" ) )
								.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/off/false)" ) ) )
						.add( new SubCommand( "rotate" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set.rotate" ) )
								.add( new SubCommand( new InputValidatorBooleanOption( new String[] { "on", "true" }, new String[] { "off", "false" }, new String[] { "unset" } ) )
										.defaultTo( this::set ) )
								.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid value! (on/true/unset/off/false)" ) )
								.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a value! (on/true/unset/off/false)" ) ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( new SubCommand( "showname" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get.showname" ) )
								.defaultTo( this::get ) )
						.add( new SubCommand( "cursor" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get.cursor" ) )
								.defaultTo( this::get ) )
						.add( new SubCommand( "rotate" )
								.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get.rotate" ) )
								.defaultTo( this::get ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid setting!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide a setting!" ) ) )
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
	
	private void set( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		String property = parameters.getLast( String.class );
		
		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( property.equalsIgnoreCase( "cursor" ) ) {
			boolean isOn = parameters.getLast( boolean.class );
			
			viewer.setCursorActive( isOn );
			sender.sendMessage( ChatColor.GREEN + "Set cursor to " + ChatColor.LIGHT_PURPLE + ( isOn ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "rotate" ) ) {
			BooleanOption option = parameters.getLast( BooleanOption.class );
			
			String rotation = "off";
			if ( option.isTrue() ) {
				rotation = "on";
			} else if ( option.isUnset() ) {
				rotation = "unset";
			}
			viewer.setRotate( option );
			sender.sendMessage( ChatColor.GREEN + "Set rotation to " + ChatColor.LIGHT_PURPLE + rotation + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "showname" ) ) {
			boolean isOn = parameters.getLast( boolean.class );
			
			viewer.setShowName( isOn );
			sender.sendMessage( ChatColor.GREEN + "Set show name to " + ChatColor.LIGHT_PURPLE + ( isOn ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		}
	}
	
	private void get( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		String property = parameters.getLast( String.class );
		
		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( property.equalsIgnoreCase( "cursor" ) ) {
			sender.sendMessage( ChatColor.GREEN + "Your cursor is " + ChatColor.LIGHT_PURPLE + ( viewer.isCursorActive() ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "rotate" ) ) {
			String rotation = "off";
			if ( viewer.getRotate().isTrue() ) {
				rotation = "on";
			} else if ( viewer.getRotate().isUnset() ) {
				rotation = "unset";
			}
			sender.sendMessage( ChatColor.GREEN + " Your rotation is " + ChatColor.LIGHT_PURPLE + rotation + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "showname" ) ) {
			sender.sendMessage( ChatColor.GREEN + "Show name is turned " + ChatColor.LIGHT_PURPLE + ( viewer.isShowName() ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		}
	}
	
	private void setOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.getLast( String.class );
		
		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( property.equalsIgnoreCase( "cursor" ) ) {
			boolean isOn = parameters.getLast( boolean.class );
			
			viewer.setCursorActive( isOn );
			sender.sendMessage( ChatColor.GREEN + "Set cursor for " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " to " + ChatColor.LIGHT_PURPLE + ( isOn ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "rotate" ) ) {
			BooleanOption option = parameters.getLast( BooleanOption.class );
			
			String rotation = "off";
			if ( option.isTrue() ) {
				rotation = "on";
			} else if ( option.isUnset() ) {
				rotation = "unset";
			}
			viewer.setRotate( option );
			sender.sendMessage( ChatColor.GREEN + "Set rotation for " +ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " to " + ChatColor.LIGHT_PURPLE + rotation + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "showname" ) ) {
			boolean isOn = parameters.getLast( boolean.class );
			
			viewer.setShowName( isOn );
			sender.sendMessage( ChatColor.GREEN + "Set show name for " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " to " + ChatColor.LIGHT_PURPLE + ( isOn ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
		}
	}

	private void getOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.getLast( String.class );
		
		MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( property.equalsIgnoreCase( "cursor" ) ) {
			sender.sendMessage( ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " has their cursor " + ChatColor.LIGHT_PURPLE + ( viewer.isCursorActive() ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "rotate" ) ) {
			String rotation = "off";
			if ( viewer.getRotate().isTrue() ) {
				rotation = "on";
			} else if ( viewer.getRotate().isUnset() ) {
				rotation = "unset";
			}
			sender.sendMessage( ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " has their rotation " + ChatColor.LIGHT_PURPLE + rotation + ChatColor.GREEN + "." );
		} else if ( property.equalsIgnoreCase( "showname" ) ) {
			sender.sendMessage( ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " has show name is turned " + ChatColor.LIGHT_PURPLE + ( viewer.isShowName() ? "on" : "off" ) + ChatColor.GREEN + "." );
		} else {
			sender.sendMessage( ChatColor.RED + "'" + property + "' has not been implemented yet! Please contact the developers for assistance!" );
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
