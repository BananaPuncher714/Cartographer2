package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

public class MapSettings {
	protected Set< ZoomScale > allowedZooms = new HashSet< ZoomScale >();
	protected ZoomScale defaultZoom = ZoomScale.ONE;
	protected boolean circularZoom = false;
	
	protected MinimapPalette palette;
	
	public MapSettings( FileConfiguration config ) {
		defaultZoom = ZoomScale.valueOf( config.getString( "default-zoom", "ONE" ).toUpperCase() );
		circularZoom = config.getBoolean( "circular-zoom", false );
		for ( String string : config.getStringList( "allowed-zooms" ) ) {
			allowedZooms.add( ZoomScale.valueOf( string.toUpperCase() ) );
		}
		
		palette = Cartographer.getInstance().getPaletteManager().construct( config.getStringList( "palettes" ) );
	}
	
	public ZoomScale getDefaultZoom() {
		return defaultZoom;
	}
	
	public void setDefaultZoom( ZoomScale defaultZoom ) {
		this.defaultZoom = defaultZoom;
	}
	
	public boolean isCircularZoom() {
		return circularZoom;
	}
	
	public void setCircularZoom( boolean circular ) {
		circularZoom = circular;
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}
	
	public void setPalette( MinimapPalette palette ) {
		this.palette = palette;
	}
}
