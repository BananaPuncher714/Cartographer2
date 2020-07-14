package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessageLocale;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModule;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModuleEnabled;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModuleUnloaded;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
import io.github.bananapuncher714.cartographer.core.module.Module;

/**
 * Cartographer2 Module subcommand.
 * 
 * @author BananaPuncher714
 */
public class CommandModule {
	private Cartographer plugin;
	private SubCommand moduleCommand;
	
	protected CommandModule( Cartographer plugin ) {
		this.plugin = plugin;
		
		moduleCommand = new SubCommand( "module" )
				.addSenderValidator( new SenderValidatorPermission( "cartographer.module" ) )
				.add( new SubCommand( "list" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.list" ) )
						.defaultTo( this::list ) )
				.add( new SubCommand( "reload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.reload" ) )
						.defaultTo( this::reload ) )
				.add( new SubCommand( "enable" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.enable" ) )
						.add( new SubCommand( new InputValidatorModuleEnabled( plugin, false ) )
								.defaultTo( this::enable ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_MESSAGE_ALREADY_ENABLED ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_ENABLE_USAGE ) ) )
				.add( new SubCommand( "disable" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.disable" ) )
						.add( new SubCommand( new InputValidatorModuleEnabled( plugin, true ) )
								.defaultTo( this::disable ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_MESSAGE_ALREADY_DISABLED ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_DISABLE_USAGE ) ) )
				.add( new SubCommand( "load" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.load" ) )
						.add( new SubCommand( new InputValidatorModuleUnloaded( plugin ) )
								.defaultTo( this::load ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_MESSAGE_INVALID_FILE ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_LOAD_USAGE ) ) )
				.add( new SubCommand( "unload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.unload" ) )
						.add( new SubCommand( new InputValidatorModule( plugin ) )
								.defaultTo( this::unload ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_MESSAGE_NOT_REAL ) )
						.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_UNLOAD_USAGE ) ) )
				.add( new SubCommand( "help" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.help" ) )
						.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MODULE_HELP_USAGE ) )
						.defaultTo( this::help ) )
				.whenUnknown( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_INVALID_ARGUMENT ) )
				.defaultTo( new CommandExecutableMessageLocale( LocaleConstants.COMMAND_MESSAGE_PROVIDE_ARGUMENT ) );
	}
	
	protected SubCommand getCommand() {
		return moduleCommand;
	}
	
	private void list( CommandSender sender, String[] args, CommandParameters parameters ) {
		Set< Module > modules = plugin.getModuleManager().getModules();
		if ( modules.isEmpty() ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_LIST_EMPTY );
		} else {
			StringBuilder builder = new StringBuilder();
			for ( Iterator< Module > iterator = modules.iterator(); iterator.hasNext(); ) {
				Module module = iterator.next();
				if ( module.isEnabled() ) {
					builder.append( ChatColor.GREEN );
				} else {
					builder.append( ChatColor.RED );
				}
				builder.append( module.getName() );
				
				if ( iterator.hasNext() ) {
					builder.append( ChatColor.GOLD );
					builder.append( ", " );
				}
			}

			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_LIST_FORMAT, modules.size(), builder.toString() );
		}
	}
	
	private void reload( CommandSender sender, String[] args, CommandParameters parameters ) {
		plugin.getModuleManager().reload();
		plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_RELOAD_SUCESS );
	}
	
	private void enable( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		if ( module.isEnabled() ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_ENABLE_ENABLE_ERROR, moduleName );
			return;
		}
		
		boolean valid = plugin.getModuleManager().enableModule( module );
		
		if ( valid ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_ENABLE_SUCCESS, moduleName );
		} else {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_ENABLE_LOAD_ERROR, moduleName );
		}
	}
	
	private void disable( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		boolean valid = plugin.getModuleManager().disableModule( module );
		
		if ( valid ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_DISABLE_SUCCESS, moduleName );
		} else {
			// Shouldn't occur with the current input validator for the disable command
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_DISABLE_ERROR, moduleName );
		}
	}
	
	private void load( CommandSender sender, String[] args, CommandParameters parameters ) {
		File file = parameters.getLast( File.class );
		Module module = plugin.getModuleManager().loadModule( file );
		if ( module == null ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_LOAD_LOAD_ERROR, file.getName() );
			return;
		}
		
		plugin.getModuleManager().registerModule( module );

		boolean valid = plugin.getModuleManager().enableModule( module );
		
		if ( valid ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_LOAD_SUCCESS, module.getName() );
		} else {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_LOAD_ENABLE_ERROR, module.getName() );
		}
	}
	
	private void unload( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		if ( plugin.getModuleManager().unloadModule( module, true ) ) {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_UNLOAD_SUCCESS, moduleName );
		} else {
			plugin.getLocaleManager().translateAndSend( sender, LocaleConstants.COMMAND_MODULE_UNLOAD_ERROR, moduleName );
		}
	}
	
	private void help( CommandSender sender, String[] args, CommandParameters parameters ) {
		for ( int i = 0; i < 20; i++ ) {
			plugin.getLocaleManager().translateAndSend( sender, String.format( LocaleConstants.COMMAND_MODULE_HELP_FORMAT, i ) );
		}
	}
}
