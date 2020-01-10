package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.bananapuncher714.cartographer.core.util.FailSafe;

public class SubCommand implements CommandExecutor, TabCompleter {
	protected CommandValidator validator;
	protected List< SubCommand > subCommands = new ArrayList< SubCommand >();
	
	protected CommandExecutable whenUnknown;
	protected CommandExecutable whenNone;
	
	public SubCommand( CommandValidator validator ) {
		this.validator = validator;
	}
	
	public SubCommand apply( SubCommand builder ) {
		return this;
	}
	
	public SubCommand setCommandValidator( CommandValidator validator ) {
		this.validator = validator;
		return this;
	}
	
	public SubCommand whenUnknown( CommandExecutable executable ) {
		this.whenUnknown = executable;
		return this;
	}
	
	public SubCommand whenNone( CommandExecutable executable ) {
		this.whenNone = executable;
		return this;
	}
	
	public boolean validate( CommandSender sender ) {
		return true;
	}
	
	public boolean matches( String input ) {
		return validator.isValid( input );
	}
	
	public Collection< String > getSuggestions() {
		return validator.getTabCompletes();
	}
	
	public void submit( CommandSender sender, String command, String[] args ) {
		if ( args.length > 0 ) {
			SubCommand commandMatch = null;
			String arg = args[ 0 ];
			for ( SubCommand subCommand : subCommands ) {
				if ( subCommand.matches( arg ) && subCommand.validate( sender ) ) {
					commandMatch = subCommand;
					break;
				}
			}
			if ( commandMatch != null ) {
				commandMatch.submit( sender, arg, FailSafe.pop( args ) );
			} else {
				whenUnknown.execute( sender, args );
			}
		} else {
			whenNone.execute( sender, args );
		}
	}

	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		return null;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		return false;
	}
}
