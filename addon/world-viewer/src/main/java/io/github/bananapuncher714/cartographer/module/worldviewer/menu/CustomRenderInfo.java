package io.github.bananapuncher714.cartographer.module.worldviewer.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.DataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.renderer.RenderInfo;

public class CustomRenderInfo extends RenderInfo {
	public CustomRenderInfo() {
		this.mapCursors = new ArrayList< MapCursor >();
		this.mapPixels = new ArrayList< MapPixel >();
		this.worldCursors = new ArrayList< WorldCursor >();
		this.worldPixels = new ArrayList< WorldPixel >();
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int[] getUpperPixelInfo() {
		return upperPixelInfo;
	}

	public void setUpperPixelInfo( int[] upperPixelInfo ) {
		this.upperPixelInfo = upperPixelInfo;
	}

	public int[] getLowerPixelInfo() {
		return lowerPixelInfo;
	}

	public void setLowerPixelInfo( int[] lowerPixelInfo ) {
		this.lowerPixelInfo = lowerPixelInfo;
	}

	public MapCursor[] getCursors() {
		return cursors;
	}

	public void setCursors( MapCursor[] cursors ) {
		this.cursors = cursors;
	}

	public Set< BigChunkLocation > getNeedsRender() {
		return needsRender;
	}

	public void setNeedsRender( Set< BigChunkLocation > needsRender ) {
		this.needsRender = needsRender;
	}

	public Collection<WorldPixel> getWorldPixels() {
		return worldPixels;
	}

	public void setWorldPixels( Collection< WorldPixel > worldPixels ) {
		this.worldPixels = worldPixels;
	}

	public Collection<MapPixel> getMapPixels() {
		return mapPixels;
	}

	public void setMapPixels( Collection< MapPixel > mapPixels ) {
		this.mapPixels = mapPixels;
	}

	public Collection<WorldCursor> getWorldCursors() {
		return worldCursors;
	}

	public void setWorldCursors( Collection< WorldCursor > worldCursors ) {
		this.worldCursors = worldCursors;
	}

	public Collection<MapCursor> getMapCursors() {
		return mapCursors;
	}

	public void setMapCursors( Collection< MapCursor > mapCursors ) {
		this.mapCursors = mapCursors;
	}

	public PlayerSetting getSetting() {
		return setting;
	}

	public void setSetting( PlayerSetting setting ) {
		this.setting = setting;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID( UUID uuid ) {
		this.uuid = uuid;
	}

	public Minimap getMap() {
		return map;
	}

	public void setMap( Minimap map ) {
		this.map = map;
	}

	public DataCache getCache() {
		return cache;
	}

	public void setCache( DataCache cache ) {
		this.cache = cache;
	}

	public SimpleImage getOverlayImage() {
		return overlayImage;
	}

	public void setOverlayImage( SimpleImage overlayImage ) {
		this.overlayImage = overlayImage;
	}

	public SimpleImage getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage( SimpleImage backgroundImage ) {
		this.backgroundImage = backgroundImage;
	}
}
