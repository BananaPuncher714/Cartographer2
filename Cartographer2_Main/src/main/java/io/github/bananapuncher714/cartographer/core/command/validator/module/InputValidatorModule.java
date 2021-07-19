package io.github.bananapuncher714.cartographer.core.command.validator.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class InputValidatorModule implements InputValidator< Module > {
	protected Cartographer plugin;
	
	public InputValidatorModule( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		Set< String > names = new HashSet< String >();
		for ( Module module : plugin.getModuleManager().getModules() ) {
			names.add( module.getName() );
		}
		return names;
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input, String[] args ) {
		return plugin.getModuleManager().getModule( input[ 0 ] ) != null;
	}

	@Override
	public Module get( CommandSender sender, String[] input ) {
		return plugin.getModuleManager().getModule( input[ 0 ] );
	}
}
