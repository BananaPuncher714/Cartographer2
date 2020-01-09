package io.github.bananapuncher714.cartographer.core.map;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;

/**
 * Contains settings for a {@link Minimap} that can be loaded from a config.
 * 
 * @author BananaPuncher714
 */
public class MapSettings {
	// Zoom settings
	protected Set< ZoomScale > allowedZooms = new HashSet< ZoomScale >();
	protected ZoomScale defaultZoom = ZoomScale.ONE;
	protected boolean circularZoom = false;
	
	// Auto update the map as needed
	protected boolean autoUpdate = true;
	
	// Render chunks that are not within the border, either defined by WorldBorder or by the vanilla world border
	protected boolean renderOutOfBorder = false;

	// Default rotation option
	protected BooleanOption rotation = BooleanOption.UNSET;
	
	// Default palette
	protected MinimapPalette palette;
	
	public MapSettings( FileConfiguration config ) {
		defaultZoom = ZoomScale.valueOf( config.getString( "default-zoom", "ONE" ).toUpperCase() );
		circularZoom = config.getBoolean( "circular-zoom", false );
		autoUpdate = config.getBoolean( "auto-update", true );
		rotation = FailSafe.getEnum( BooleanOption.class, config.getString( "rotate", "UNSET" ) );
		renderOutOfBorder = config.getBoolean( "render-out-of-border", false );
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
	
	public boolean isRenderOutOfBorder() {
		return renderOutOfBorder;
	}
	
	public void setRenderOutOfBorder( boolean render ) {
		renderOutOfBorder = render;
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
