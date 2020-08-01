package io.github.bananapuncher714.cartographer.core.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

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
		return 0;
	}
	
	public double getNextZoom( double currentZoom ) {
		return 0;
	}
	
	public double getFarthestZoom() {
		return 0;
	}
}
