package io.github.bananapuncher714.cartographer.core.command.validator.module;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class InputValidatorModuleUnloaded implements InputValidator< File > {
	protected Cartographer plugin;
	
	public InputValidatorModuleUnloaded( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		Set< String > files = new HashSet< String >();
		for ( File file : Cartographer.getModuleDir().listFiles() ) {
			if ( file.exists() && file.isFile() && file.getName().matches( ".*?\\.jar$" ) ) {
				boolean found = false;
				for ( Module module : plugin.getModuleManager().getModules() ) {
					File moduleFile = module.getFile();
					if ( moduleFile.getAbsolutePath().equals( file.getAbsolutePath() ) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					files.add( file.getName() );
				}
			}
		}
		return files;
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input , String[] args ) {
		File file = new File( Cartographer.getModuleDir() + "/" + input[ 0 ].replace( "/", "" ) );
		if ( !( file.exists() && file.isFile() && file.getName().matches( ".*?\\.jar$" ) ) ) {
			return false;
		}
		
		for ( Module module : plugin.getModuleManager().getModules() ) {
			if ( file.getAbsolutePath().equals( module.getFile().getAbsolutePath() ) ) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public File get( CommandSender sender, String[] input ) {
		return new File( Cartographer.getModuleDir() + "/" + input[ 0 ].replace( "/", "" ) );
	}
}
