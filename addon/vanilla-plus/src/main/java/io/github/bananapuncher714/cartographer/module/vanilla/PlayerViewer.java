package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorConverter;

public class PlayerViewer {
	private VanillaPlus module;
	private List< CursorConverter > converters = new ArrayList< CursorConverter >();

	public PlayerViewer( VanillaPlus module ) {
		this.module = module;
	}
	
	public WorldCursor convert( Object object, Player player, PlayerSetting settings ) {
		for ( CursorConverter converter : converters ) {
			if ( converter.convertable( object ) ) {
				return converter.convert( object, player, settings );
			}
		}
		CursorConverter converter = module.getConverterFor( object );
		if ( converter != null ) {
			return converter.convert( object, player, settings );
		}
		return null;
	}
	
	public void addConverter( CursorConverter converter ) {
		converters.add( converter );
	}
	
	public void removeConverter( CursorConverter converter ) {
		converters.remove( converter );
	}
	
	public List< CursorConverter > getConverters() {
		return converters;
	}
}
