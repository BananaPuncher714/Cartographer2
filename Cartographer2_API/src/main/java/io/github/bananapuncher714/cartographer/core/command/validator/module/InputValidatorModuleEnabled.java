package io.github.bananapuncher714.cartographer.core.command.validator.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class InputValidatorModuleEnabled implements InputValidator< Module > {
	protected Cartographer plugin;
	protected boolean enabled = true;
	
	public InputValidatorModuleEnabled( Cartographer plugin, boolean enabled ) {
		this.plugin = plugin;
		this.enabled = enabled;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		Set< String > names = new HashSet< String >();
		for ( Module module : plugin.getModuleManager().getModules() ) {
			if ( module.isEnabled() == enabled ) {
				names.add( module.getName() );
			}
		}
		return names;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		Module module = plugin.getModuleManager().getModule( input );
		return module != null && module.isEnabled() == enabled;
	}

	@Override
	public Module get( String input ) {
		return plugin.getModuleManager().getModule( input );
	}
}
