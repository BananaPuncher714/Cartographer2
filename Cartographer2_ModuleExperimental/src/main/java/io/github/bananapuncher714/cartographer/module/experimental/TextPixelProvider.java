package io.github.bananapuncher714.cartographer.module.experimental;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.map.MapPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class TextPixelProvider implements MapPixelProvider {
	private ExperimentalModule module;
	
	public TextPixelProvider( ExperimentalModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< MapPixel > getMapPixels( Player player, Minimap map, PlayerSetting setting ) {
		Set< MapPixel > pixels = new HashSet< MapPixel >();
		
		String string = module.getTestString();
		if ( string != null ) {
			pixels.addAll( module.getFor( string, 4, 20, module.getColor() ) );
		}
		pixels.forEach( p -> p.setPriority( 0xFFFF ) );
		
		return pixels;
	}
}
