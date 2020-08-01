package io.github.bananapuncher714.cartographer.core.command.validator;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;

public class InputValidatorMinimapFile implements InputValidator< File > {
	protected Cartographer plugin;
	
	public InputValidatorMinimapFile( Cartographer plugin ) {
		this.plugin = plugin;
	}

	@Override
	public Collection< String > getTabCompletes() {
		Set< String > values = new HashSet< String >();
		for ( File file : Cartographer.getMapSaveDir().listFiles() ) {
			if ( !file.isDirectory() ) {
				continue;
			}

			String name = file.getName();
			if ( !plugin.getMapManager().getMinimaps().containsKey( name ) ) {
				values.add( name );
			}
		}
		return values;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		File file = new File( Cartographer.getMapSaveDir() + "/" + input.replace( "/", "" ) );
		if ( !file.exists() || !file.isDirectory() ) {
			return false;
		}
		return !plugin.getMapManager().getMinimaps().containsKey( file.getName() );
	}

	@Override
	public File get( String input ) {
		return new File( Cartographer.getMapSaveDir() + "/" + input.replace( "/", "" ) );
	}
}
