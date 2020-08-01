package io.github.bananapuncher714.cartographer.core.command.validator.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class InputValidatorModule implements InputValidator< Module > {
	protected Cartographer plugin;
	
	public InputValidatorModule( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		Set< String > names = new HashSet< String >();
		for ( Module module : plugin.getModuleManager().getModules() ) {
			names.add( module.getName() );
		}
		return names;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		return plugin.getModuleManager().getModule( input ) != null;
	}

	@Override
	public Module get( String input ) {
		return plugin.getModuleManager().getModule( input );
	}
}
