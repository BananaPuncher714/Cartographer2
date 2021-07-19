package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public boolean matches( CommandSender sender, String input[], String[] args ) {
		return ( validator == null ? true : validator.isValid( sender, input, args ) ) && matches( sender );
	}
	
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return validator == null ? null : validator.getTabCompletes( sender, input );
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
	
	public CommandResult submit( CommandSender sender, String[] command, String[] args, CommandParameters parameter ) {
		CommandResult result = new CommandResult();
		parameter = new CommandParameters( parameter );
		if ( validator != null ) {
			parameter.add( validator.get( sender, command ) );
		} else {
			parameter.add( null );
		}
		if ( args.length > 0 ) {
			boolean found = false;
			for ( SubCommand subCommand : subCommands ) {
				SplitCommand split = split( args, subCommand.getInputValidator().getArgumentCount() );
				String[] input = split.getInput();
				String[] newArgs = split.getArguments();
				if ( subCommand.matches( sender, input, newArgs ) ) {
					found = true;
					
					result.add( subCommand.submit( sender, input, newArgs, parameter ) );
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
			for ( SubCommand subCommand : subCommands ) {
				if ( subCommand.matches( sender ) ) {
					SplitCommand split = split( args, subCommand.getInputValidator().getArgumentCount() );
					String[] input = split.getInput();
					String[] newArgs = split.getArguments();
					// Check if the subcommand matches the argument, and if it has subcommands of its own
					if ( subCommand.matches( sender, input, newArgs ) && !subCommand.getSubCommands().isEmpty() ) {
						tabs.addAll( subCommand.getTabCompletions( sender, newArgs ) );
					} else if ( newArgs.length == 0 ) {
						// If not, and this is the last argument, add all possible tab completes
						// This allows for "recommendations"
						Collection< String > completions = subCommand.getTabCompletes( sender, input );
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
			String[] commandArr = new String[ args.length + 1 ];
			
			commandArr[ 0 ] = command.getName();
			for ( int i = 1; i < commandArr.length; i++ ) {
				commandArr[ i ] = args[ i - 1 ];
			}
			
			SplitCommand split = split( commandArr, validator.getArgumentCount() );
			submit( sender, split.getInput(), split.getArguments(), parameters ).execute( sender );
		}
		return false;
	}
	
	private List< String > onTabComplete( CommandSender sender, Command command, String arg2, String[] args ) {
		List< String > completions = new ArrayList< String >();
		String[] commandArr = new String[ args.length + 1 ];
		
		commandArr[ 0 ] = command.getName();
		for ( int i = 1; i < commandArr.length; i++ ) {
			commandArr[ i ] = args[ i - 1 ];
		}
		
		SplitCommand split = split( commandArr, validator.getArgumentCount() );
		StringUtil.copyPartialMatches( args[ args.length - 1 ], getTabCompletions( sender, split.getArguments() ), completions );
		Collections.sort( completions );
		return completions;
	}
	
	protected static SplitCommand split( String[] command, int inputSize ) {
		String[] input = command.length > 0 ? Arrays.copyOfRange( command, 0, Math.min( inputSize, command.length ) ) : new String[ 0 ];
		String[] args = command.length > inputSize ? Arrays.copyOfRange( command, inputSize, command.length ) : new String[ 0 ];
		
		return new SplitCommand( input, args );
	}
}
