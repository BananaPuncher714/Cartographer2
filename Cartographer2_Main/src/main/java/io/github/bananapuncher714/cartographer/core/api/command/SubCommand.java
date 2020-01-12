package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorString;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidator;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;

public class SubCommand {
	protected List< SubCommand > subCommands = new ArrayList< SubCommand >();
	
	protected InputValidator< ? > validator;
	protected Set< SenderValidator > senderValidators = new HashSet< SenderValidator >();
	protected CommandExecutable whenUnknown;
	protected CommandExecutable whenNone;
	
	public SubCommand() {
	}
	
	// Helped constructor
	public SubCommand( String command ) {
		this( new InputValidatorString( command ) );
	}
	
	public SubCommand( InputValidator< ? > validator ) {
		this.validator = validator;
	}
	
	public SubCommand add( SubCommand builder ) {
		subCommands.add( builder );
		return this;
	}
	
	public SubCommand addSenderValidator( SenderValidator validator ) {
		senderValidators.add( validator );
		return this;
	}
	
	public SubCommand whenUnknown( CommandExecutable executable ) {
		this.whenUnknown = executable;
		return this;
	}
	
	// Naming by jetp250
	public SubCommand defaultTo( CommandExecutable executable ) {
		this.whenNone = executable;
		return this;
	}
	
	public boolean matches( CommandSender sender ) {
		for ( SenderValidator validator : senderValidators ) {
			if ( !validator.isValid( sender ) ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean matches( String input, String[] args ) {
		return validator == null ? true : validator.isValid( input, args );
	}
	
	public boolean matches( CommandSender sender, String input, String[] args ) {
		return matches( input, args ) && matches( sender );
	}
	
	public Collection< String > getTabCompletes() {
		return validator == null ? null : validator.getTabCompletes();
	}
	
	public List< SubCommand > getSubCommands() {
		return subCommands;
	}
	
	public InputValidator< ? > getInputValidator() {
		return validator;
	}
	
	public Set< SenderValidator > getSenderValidators() {
		return senderValidators;
	}
	
	public CommandResult submit( CommandSender sender, String command, String[] args, CommandParameters parameter ) {
		CommandResult result = new CommandResult();
		parameter = new CommandParameters( parameter );
		if ( validator != null ) {
			parameter.add( validator.get( command ) );
		} else {
			parameter.add( null );
		}
		if ( args.length > 0 ) {
			String arg = args[ 0 ];
			
			// Right here, each sub command may match it, so we want to get all the subcommands of each one
			// Build a new subcommand?
			boolean found = false;
			String[] newArgs = FailSafe.pop( args );
			for ( SubCommand subCommand : subCommands ) {
				if ( subCommand.matches( sender, arg, FailSafe.pop( args ) ) ) {
					found = true;
					
					result.add( subCommand.submit( sender, arg, newArgs, parameter ) );
				}
			}
			if ( !found ) {
				if ( whenUnknown != null ) {
					result.add( new CommandOption( whenUnknown, args, parameter ) );
				} else if ( whenNone != null ) {
					result.add( new CommandOption( whenNone, args, parameter ) );
				}
			}
		} else {
			if ( whenNone != null ) {
				result.add( new CommandOption( whenNone, args, parameter ) );
			}
		}
		return result;
	}
	
	public Collection< String > getTabCompletions( CommandSender sender, String[] args ) {
		Set< String > tabs = new HashSet< String >();
		if ( args.length > 0 ) {
			String arg = args[ 0 ];
			String[] newArgs = FailSafe.pop( args );
			for ( SubCommand subCommand : subCommands ) {
				if ( subCommand.matches( sender ) ) {
					if ( subCommand.matches( arg, newArgs ) ) {
						tabs.addAll( subCommand.getTabCompletions( sender, newArgs ) );
					} else if ( args.length == 1 ) {
						Collection< String > completions = subCommand.getTabCompletes();
						if ( completions != null ) {
							tabs.addAll( completions );
						}
					}
				}
			}
		}
		return tabs;
	}
	
	public SubCommand apply( PluginCommand command ) {
		command.setExecutor( this::onCommand );
		command.setTabCompleter( this::onTabComplete );
		return this;
	}

	private boolean onCommand( CommandSender sender, Command command, String arg2, String[] args ) {
		CommandParameters parameters = new CommandParameters();
		submit( sender, command.getName(), args, parameters ).execute( sender );
		return false;
	}
	
	private List< String > onTabComplete( CommandSender sender, Command command, String arg2, String[] args ) {
		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], getTabCompletions( sender, args ), completions );
		Collections.sort( completions );
		return completions;
	}
}
