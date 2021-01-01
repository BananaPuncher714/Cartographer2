package io.github.bananapuncher714.cartographer.core.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;

import io.github.bananapuncher714.cartographer.core.api.configuration.YamlComment;
import io.github.bananapuncher714.cartographer.core.api.configuration.YamlFileConfiguration;
import io.github.bananapuncher714.cartographer.core.api.configuration.YamlKey;

public class YamlMerger {
	protected YamlFileConfiguration config;
	protected YamlFileConfiguration internal;
	
	public YamlMerger( YamlFileConfiguration config, InputStream stream ) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.config = config;
		
		internal = new YamlFileConfiguration( config.getFile() );
		internal.load( stream );
	}

	public YamlMerger updateKeys() {
		for ( String key : internal.getConfiguration().getKeys( true ) ) {
			if ( !config.getConfiguration().contains( key ) ||
					config.getConfiguration().get( key ).getClass() != internal.getConfiguration().get( key ).getClass() ) {
				config.getConfiguration().set( key, internal.getConfiguration().get( key ) );
			}
		}
		return this;
	}
	
	public YamlMerger trimKeys() {
		for ( String key : config.getConfiguration().getKeys( true ) ) {
			if ( !internal.getConfiguration().contains( key ) ) {
				config.getConfiguration().set( key, null );
			}
		}
		return this;
	}
	
	public YamlMerger updateHeader( boolean force ) {
		if ( !internal.getHeader().isEmpty() ) {
			if ( force || config.getHeader().isEmpty() ) {
				config.setHeader( internal.getHeader() );
			}
		}
		return this;
	}
	
	public YamlMerger updateComments( boolean force ) {
		for ( Entry< List< YamlKey >, YamlComment > entry : internal.getComments().entrySet() ) {
			List< YamlKey > key = entry.getKey();
			YamlComment comment = entry.getValue();
			if ( force ) {
				config.setComment( key, comment.copyOf() );
			} else {
				YamlComment original = config.getComment( key );
			
				if ( original == null ) {
					config.setComment( key, comment.copyOf() );
				} else if ( original.isEmpty() ) {
					original.getComments().addAll( comment.getComments() );
				}
			}
		}
		return this;
	}
}
