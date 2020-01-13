package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPermission;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModule;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModuleEnabled;
import io.github.bananapuncher714.cartographer.core.command.validator.module.InputValidatorModuleUnloaded;
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
						.add( new SubCommand( new InputValidatorModuleEnabled( plugin, true ) )
								.defaultTo( this::enable ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "This module is already enabled or does not exist!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer module enable <module>" ) ) )
				.add( new SubCommand( "disable" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.disable" ) )
						.add( new SubCommand( new InputValidatorModuleEnabled( plugin, false ) )
								.defaultTo( this::disable ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "This module is already disabled or does not exist!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer module disable <module>" ) ) )
				.add( new SubCommand( "load" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.load" ) )
						.add( new SubCommand( new InputValidatorModuleUnloaded( plugin ) )
								.defaultTo( this::load ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "This file is already loaded or does not exist!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer module load <file>" ) ) )
				.add( new SubCommand( "unload" )
						.addSenderValidator( new SenderValidatorPermission( "cartographer.module.unload" ) )
						.add( new SubCommand( new InputValidatorModule( plugin ) )
								.defaultTo( this::unload ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "This module does not exist!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "Usage: /cartographer module unload <module>" ) ) )
				.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "Invalid argument!" ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide an argument!" ) );
	}
	
	protected SubCommand getCommand() {
		return moduleCommand;
	}
	
	private void list( CommandSender sender, String[] args, CommandParameters parameters ) {
		Set< Module > modules = plugin.getModuleManager().getModules();
		if ( modules.isEmpty() ) {
			sender.sendMessage( ChatColor.GOLD + "There are currently no modules loaded!" );
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append( ChatColor.GOLD );
			builder.append( "Cartographer2 Modules (" );
			builder.append( modules.size() );
			builder.append( "): " );
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
			sender.sendMessage( builder.toString() );
		}
	}
	
	private void reload( CommandSender sender, String[] args, CommandParameters parameters ) {
		plugin.getModuleManager().reload();
		sender.sendMessage( ChatColor.GOLD + "Reloaded all modules!" );
	}
	
	private void enable( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		if ( module.isEnabled() ) {
			sender.sendMessage( ChatColor.RED + "Module '" + moduleName + "' is already enabled!" );
			return;
		}
		
		boolean valid = plugin.getModuleManager().enableModule( module );
		
		if ( valid ) {
			sender.sendMessage( ChatColor.GOLD + "Enabled module '" + ChatColor.YELLOW + moduleName + ChatColor.GOLD + "'!" );
		} else {
			sender.sendMessage( ChatColor.RED + "Unable to load module '" + moduleName + "', Check the server log for details. (Missing dependencies?)" );
		}
	}
	
	private void disable( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		boolean valid = plugin.getModuleManager().disableModule( module );
		
		if ( valid ) {
			sender.sendMessage( ChatColor.GOLD + "Disabled module '" + ChatColor.YELLOW + moduleName + ChatColor.GOLD + "'!" );
		} else {
			// Shouldn't occur with the current input validator for the disable command
			sender.sendMessage( ChatColor.RED + "Module '" + moduleName + "' is already disabled!" );
		}
	}
	
	private void load( CommandSender sender, String[] args, CommandParameters parameters ) {
		File file = parameters.getLast( File.class );
		Module module = plugin.getModuleManager().loadModule( file );
		if ( module == null ) {
			sender.sendMessage( ChatColor.RED + "Unable to load module '" + file.getName() + "', Check the server log for details." );
			return;
		}
		
		plugin.getModuleManager().registerModule( module );

		boolean valid = plugin.getModuleManager().enableModule( module );
		
		if ( valid ) {
			sender.sendMessage( ChatColor.GOLD + "Loaded and enabled module '" + ChatColor.YELLOW + module.getName() + ChatColor.GOLD + "'!" );
		} else {
			sender.sendMessage( ChatColor.RED + "Unable to enable module '" + module.getName() + "', Check the server log for details. (Missing dependencies?)" );
		}
	}
	
	private void unload( CommandSender sender, String[] args, CommandParameters parameters ) {
		Module module = parameters.getLast( Module.class );
		String moduleName = module.getName();
		
		if ( plugin.getModuleManager().unloadModule( module ) ) {
			sender.sendMessage( ChatColor.GOLD + "Unloaded module '" + ChatColor.YELLOW + moduleName + ChatColor.GOLD + "'!" );
		} else {
			sender.sendMessage( ChatColor.GOLD + "Could not unload module '" + ChatColor.YELLOW + moduleName + ChatColor.GOLD + "'! Was not loaded by Cartographer2!" );
		}
	}
}
