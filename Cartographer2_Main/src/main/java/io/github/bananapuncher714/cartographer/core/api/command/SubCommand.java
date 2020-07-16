package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutable;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorString;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidator;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;

/**
 * A build and run command framework for automatic tab completions and easy branching.
 * 
 * @author BananaPuncher714
 */
public class SubCommand {
	protected List< SubCommand > subCommands = new ArrayList< SubCommand >();
	
	protected InputValidator< ? > validator;
	protected Set< SenderValidator > senderValidators = new HashSet< SenderValidator >();
	protected CommandExecutable whenUnknown;
	protected CommandExecutable whenNone;
	
	/**
	 * Accept anything as a valid input
	 */
	public SubCommand() {
	}
	
	// Helped constructor, really belongs in a factory.
	/**
	 * Create a SubCommand with the string as the input validator.
	 * 
	 * @param command
	 * The subcommand value.
	 */
	public SubCommand( String command ) {
		this( new InputValidatorString( command ) );
	}
	
	/**
	 * Create a SubCommand with the input validator provided.
	 * 
	 * @param validator
	 * Can be null.
	 */
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
	
	/**
	 * Ran when the arguments provided don't match any SubCommands registered.
	 * 
	 * @param executable
	 * An executable where the arguments will start with the unknown subcommand.
	 * @return
	 * Builder pattern return.
	 */
	public SubCommand whenUnknown( CommandExecutable executable ) {
		this.whenUnknown = executable;
		return this;
	}
	
	// Naming by jetp250
	/**
	 * Ran when there are no arguments provided, or if the executable for when unknown is not set.
	 * 
	 * @param executable
	 * If null, nothing will happen.
	 * @return
	 * Builder pattern return.
	 */
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
					// Check if the subcommand matches the argument, and if it has subcommands of its own
					if ( subCommand.matches( arg, newArgs ) && !subCommand.getSubCommands().isEmpty() ) {
						tabs.addAll( subCommand.getTabCompletions( sender, newArgs ) );
					} else if ( args.length == 1 ) {
						// If not, and this is the last argument, add all possible tab completes
						// This allows for "recommendations"
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
	
	public SubCommand applyTo( PluginCommand command ) {
		command.setExecutor( this::onCommand );
		command.setTabCompleter( this::onTabComplete );
		return this;
	}

	private boolean onCommand( CommandSender sender, Command command, String arg2, String[] args ) {
		CommandParameters parameters = new CommandParameters();
		if ( matches( sender ) ) {
			submit( sender, command.getName(), args, parameters ).execute( sender );
		}
		return false;
	}
	
	private List< String > onTabComplete( CommandSender sender, Command command, String arg2, String[] args ) {
		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], getTabCompletions( sender, args ), completions );
		Collections.sort( completions );
		return completions;
	}
}
