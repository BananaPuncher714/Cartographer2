package io.github.bananapuncher714.cartographer.core.command;

import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessageLocale;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorSettingState;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorNotPlayer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
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
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_SETTING ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_SETTING ) );


		SubCommand getOther = new SubCommand( new InputValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_SETTING ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_SETTING ) );

		SubCommand set = new SubCommand( "set" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set" ) )
				.addSenderValidator( new SenderValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_SETTING ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_SETTING ) );

		SubCommand get = new SubCommand( "get" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.get" ) )
				.addSenderValidator( new SenderValidatorPlayer() )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_SETTING ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_SETTING ) );

		for ( SettingState< ? > state : MapViewer.getStates() ) {
			if ( !state.isPrivate() ) {
				String id = state.getId();
				setOther.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother." + id ) )
						.add( new SubCommand( new InputValidatorSettingState( state ) )
								.defaultTo( this::setOther ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_MESSAGE_INVALID, state.getValues().stream().collect( Collectors.joining( "/" ) ) ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_MESSAGE_PROVIDE, state.getValues().stream().collect( Collectors.joining( "/" ) ) ) ) );

				getOther.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother." + id ) )
						.defaultTo( this::getOther ) );

				set.add( new SubCommand( id )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.set." + id ) )
						.add( new SubCommand( new InputValidatorSettingState( state ) )
								.defaultTo( this::set ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_MESSAGE_INVALID, state.getValues().stream().collect( Collectors.joining( "/" ) ) ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_MESSAGE_PROVIDE, state.getValues().stream().collect( Collectors.joining( "/" ) ) ) ) );

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
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PLAYER_MISSING ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_SET_CONSOLE ) ) )
				.add( new SubCommand( "get" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother" ) )
						.addSenderValidator( new SenderValidatorNotPlayer() )
						.add( getOther )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PLAYER_MISSING ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_GET_CONSOLE ) ) )
				.add( set )
				.add( get )
				.add( new SubCommand( "setother" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.setother" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( setOther )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PLAYER_MISSING ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_SETOTHER_USAGE ) ) )
				.add( new SubCommand( "getother" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.getother" ) )
						.addSenderValidator( new SenderValidatorPlayer() )
						.add( getOther )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PLAYER_MISSING ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_GETOTHER_USAGE ) ) )
				.add( new SubCommand( "help" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.settings.help" ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_SETTING_HELP_USAGE ) )
						.defaultTo( this::help ) )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_ARGUMENT ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_ARGUMENT ) );
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
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_MESSAGE_UNIMPLEMENTED, property );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			Optional< T > optional = state.getFrom( value );
			viewer.setSetting( state, optional.isPresent() ? optional.get() : state.getDefault() );
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_SET_SUCCESS, state.getId(), state.convertToString( viewer.getSetting( state ) ) );
		}
	}
	
	private < T extends Comparable< T > > void get( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		String property = parameters.getLast( String.class );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_MESSAGE_UNIMPLEMENTED, property );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_GET_SUCCESS, state.getId(), state.convertToString( viewer.getSetting( state ) ) );
		}
	}
	
	private < T extends Comparable< T > > void setOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.get( String.class, 4 );
		String value = parameters.get( String.class, 5 );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_MESSAGE_UNIMPLEMENTED, property );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			Optional< T > optional = state.getFrom( value );
			viewer.setSetting( state, optional.isPresent() ? optional.get() : state.getDefault() );
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_SETOTHER_SUCCESS, player.getName(), state.getId(), state.convertToString( viewer.getSetting( state ) ) );
		}
	}

	private < T extends Comparable< T > > void getOther( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = parameters.getLast( Player.class );
		String property = parameters.getLast( String.class );
		
		SettingState< T > state = ( SettingState< T > ) MapViewer.getState( property );
		if ( state == null ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_MESSAGE_UNIMPLEMENTED, property );
		} else {
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_SETTING_GETOTHER_SUCCESS, player.getName(), state.getId(), state.convertToString( viewer.getSetting( state ) ) );
		}
	}
	
	private void help( CommandSender sender, String[] args, CommandParameters parameters ) {
		for ( int i = 0; i < 20; i++ ) {
			plugin.getLocaleManager().translateAndSend( sender, String.format( LocaleConstants.COMMAND_SETTING_HELP_FORMAT, i ) );
		}
	}
}
