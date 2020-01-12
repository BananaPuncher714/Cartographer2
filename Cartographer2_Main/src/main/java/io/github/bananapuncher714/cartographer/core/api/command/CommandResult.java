package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;

public class CommandResult {
	protected List< CommandOption > options = new ArrayList< CommandOption >();
	
	public void add( CommandOption option ) {
		options.add( option );
	}
	
	public void addAll( Collection< CommandOption > options ) {
		this.options.addAll( options );
	}
	
	public void add( CommandResult result ) {
		this.options.addAll( result.getOptions() );
	}
	
	public List< CommandOption > getOptions() {
		return options;
	}
	
	public void execute( CommandSender sender ) {
		CommandOption lowest = null;
		for ( CommandOption option : options ) {
			if ( lowest == null || option.getArgumentSize() < lowest.getArgumentSize() ) {
				lowest = option;
			}
		}
		if ( lowest != null ) {
			lowest.execute( sender );
		}
	}
}
