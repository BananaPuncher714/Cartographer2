package io.github.bananapuncher714.cartographer.core.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	protected List< Double > allowedZooms = new ArrayList< Double >();
	protected double defaultZoom = 1;
	protected boolean circularZoom = false;
	
	protected boolean isWhitelist = false;
	protected Set< String > blacklistedWorlds = new HashSet< String >();
	
	// Auto update the map as needed
	protected boolean autoUpdate = true;
	
	// Render chunks that are not within the border, either defined by WorldBorder or by the vanilla world border
	protected boolean renderOutOfBorder = false;

	// Default rotation option
	protected BooleanOption rotation = BooleanOption.UNSET;
	
	// If the images should be dithered
	protected boolean ditherOverlay = false;
	protected boolean ditherBackground = false;
	protected boolean ditherDisabled = false;
	
	// Default palette
	protected MinimapPalette palette;
	
	public MapSettings( FileConfiguration config ) {
		String defaultZoomConfigVal = config.getString( "default-zoom", "1" );
		try {
			defaultZoom = Double.valueOf( defaultZoomConfigVal );
		} catch ( NumberFormatException exception ) {
			defaultZoom = ZoomScale.valueOf( defaultZoomConfigVal.toUpperCase() ).getBlocksPerPixel();
		}
		circularZoom = config.getBoolean( "circular-zoom", false );
		autoUpdate = config.getBoolean( "auto-update", true );
		rotation = FailSafe.getEnum( BooleanOption.class, config.getString( "rotate", "UNSET" ).toUpperCase() );
		renderOutOfBorder = config.getBoolean( "render-out-of-border", false );
		for ( String string : config.getStringList( "allowed-zooms" ) ) {
			double zoomVal = 1;
			try {
				zoomVal = Double.valueOf( string );
			} catch ( NumberFormatException exception ) {
				zoomVal = ZoomScale.valueOf( string.toUpperCase() ).getBlocksPerPixel();
			}
			allowedZooms.add( zoomVal );
		}
		
		isWhitelist = config.getBoolean( "world-whitelist", false );
		blacklistedWorlds.addAll( config.getStringList( "world-blacklist" ) );
		
		ditherOverlay = config.getBoolean( "dither-overlay", false );
		ditherBackground = config.getBoolean( "dither-background", false );
		ditherDisabled = config.getBoolean( "dither-blacklisted", false );
		
		palette = Cartographer.getInstance().getPaletteManager().construct( config.getStringList( "palettes" ) );
	}
	
	public double getDefaultZoom() {
		return defaultZoom;
	}
	
	public void setDefaultZoom( double defaultZoom ) {
		this.defaultZoom = defaultZoom;
	}
	
	public boolean isCircularZoom() {
		return circularZoom;
	}
	
	public void setCircularZoom( boolean circular ) {
		circularZoom = circular;
	}
	
	public boolean isValidZoom( double scale ) {
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
	
	public boolean isDitherOverlay() {
		return ditherOverlay;
	}

	public void setDitherOverlay( boolean ditherOverlay ) {
		this.ditherOverlay = ditherOverlay;
	}

	public boolean isDitherBackground() {
		return ditherBackground;
	}

	public void setDitherBackground( boolean ditherBackground ) {
		this.ditherBackground = ditherBackground;
	}

	public boolean isDitherBlacklisted() {
		return ditherDisabled;
	}

	public void setDitherBlacklisted( boolean ditherBlacklisted ) {
		this.ditherDisabled = ditherBlacklisted;
	}

	public MinimapPalette getPalette() {
		return palette;
	}
	
	public void setPalette( MinimapPalette palette ) {
		this.palette = palette;
	}

	public boolean isBlacklisted( String world ) {
		return isWhitelist ^ blacklistedWorlds.contains( world );
	}
	
	public double getPreviousZoom( double currentZoom ) {
		int index = 0;
		while ( allowedZooms.get( index ) != currentZoom && index < allowedZooms.size() ) {
			index++;
		}
		
		if ( circularZoom && index == 0 ) {
			return allowedZooms.get( allowedZooms.size() - 1 );
		} else if ( index == allowedZooms.size() ) {
			return defaultZoom;
		} else {
			return allowedZooms.get( Math.max( 0, index - 1 ) );
		}
	}
	
	public double getNextZoom( double currentZoom ) {
		int index = 0;
		while ( allowedZooms.get( index ) != currentZoom && index < allowedZooms.size() ) {
			index++;
		}
		
		if ( circularZoom && index + 1 == allowedZooms.size() ) {
			return allowedZooms.get( 0 );
		} else if ( index == allowedZooms.size() ) {
			return defaultZoom;
		} else {
			return allowedZooms.get( Math.min( allowedZooms.size() - 1, index + 1 ) );
		}
	}
	
	public double getFarthestZoom() {
		double farthest = Double.MIN_VALUE;
		for ( double z : allowedZooms ) {
			farthest = Math.max( z, farthest );
		}
		return farthest;
	}
}
