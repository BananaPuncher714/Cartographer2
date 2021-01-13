package io.github.bananapuncher714.cartographer.core.map;

import java.util.ArrayList;
import java.util.Collection;
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
	protected BooleanOption showName = BooleanOption.UNSET;
	
	// If the images should be dithered
	protected boolean ditherOverlay = false;
	protected boolean ditherBackground = false;
	protected boolean ditherDisabled = false;
	
	protected String overlayPath;
	protected String backgroundPath;
	protected String disabledPath;
	
	protected boolean reloadChunks = true;
	protected int chunkScanLimit = 20_000;
	
	// Default palette
	protected MinimapPalette palette;
	
	public MapSettings( FileConfiguration config ) {
	}
	
	public List< Double > getAllowedZooms() {
		return allowedZooms;
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
	
	public BooleanOption getShowName() {
		return showName;
	}

	public void setShowName(BooleanOption showName) {
		this.showName = showName;
	}
	
	public String getOverlayPath() {
		return overlayPath;
	}

	public void setOverlayPath( String overlayPath ) {
		this.overlayPath = overlayPath;
	}

	public boolean isDitherOverlay() {
		return ditherOverlay;
	}

	public void setDitherOverlay( boolean ditherOverlay ) {
		this.ditherOverlay = ditherOverlay;
	}

	public String getBackgroundPath() {
		return backgroundPath;
	}

	public void setBackgroundPath( String backgroundPath ) {
		this.backgroundPath = backgroundPath;
	}

	public boolean isDitherBackground() {
		return ditherBackground;
	}

	public void setDitherBackground( boolean ditherBackground ) {
		this.ditherBackground = ditherBackground;
	}

	public String getBlacklistedPath() {
		return disabledPath;
	}

	public void getBlacklistedPath( String disabledPath ) {
		this.disabledPath = disabledPath;
	}

	public boolean isDitherBlacklisted() {
		return ditherDisabled;
	}

	public void setDitherBlacklisted( boolean ditherBlacklisted ) {
		this.ditherDisabled = ditherBlacklisted;
	}
	
	public boolean isReloadChunks() {
		return reloadChunks;
	}
	
	public void setReloadChunks( boolean reload ) {
		this.reloadChunks = reload;
	}

	public int getChunkScanLimit() {
		return chunkScanLimit;
	}

	public void setChunkScanLimit( int chunkScanLimit ) {
		this.chunkScanLimit = chunkScanLimit;
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
	
	public Collection< String > getBlacklistedWorlds( String world ) {
		return blacklistedWorlds;
	}
	
	public boolean isWhitelist() {
		return isWhitelist;
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
