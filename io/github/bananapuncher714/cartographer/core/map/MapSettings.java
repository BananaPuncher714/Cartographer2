package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

public class MapSettings {
	protected Set< ZoomScale > allowedZooms = new HashSet< ZoomScale >();
	protected ZoomScale defaultZoom = ZoomScale.ONE;
	protected boolean circularZoom = false;
	protected boolean autoUpdate = true;
	
	protected BooleanOption rotation = BooleanOption.DEFAULT;
	
	protected MinimapPalette palette;
	
	public MapSettings( FileConfiguration config ) {
		defaultZoom = ZoomScale.valueOf( config.getString( "default-zoom", "ONE" ).toUpperCase() );
		circularZoom = config.getBoolean( "circular-zoom", false );
		autoUpdate = config.getBoolean( "auto-update", true );
		for ( String string : config.getStringList( "allowed-zooms" ) ) {
			allowedZooms.add( ZoomScale.valueOf( string.toUpperCase() ) );
		}
		allowedZooms.add( defaultZoom );
		
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
	
	public boolean isValidZoom( ZoomScale scale) {
		return allowedZooms.contains( scale );
	}
	
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void setAutoUpdate( boolean autoUpdate ) {
		this.autoUpdate = autoUpdate;
	}

	public BooleanOption getRotation() {
		return rotation;
	}
	
	public void setRotation( BooleanOption rotation ) {
		this.rotation = rotation;
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}
	
	public void setPalette( MinimapPalette palette ) {
		this.palette = palette;
	}
}
