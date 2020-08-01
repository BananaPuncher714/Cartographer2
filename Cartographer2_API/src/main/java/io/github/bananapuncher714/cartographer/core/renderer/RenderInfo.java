package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;

public class RenderInfo {
	// These have to be generated
	protected byte[] data;
	protected int[] upperPixelInfo;
	protected int[] lowerPixelInfo;
	protected int[] globalOverlay;
	protected int[] background;
	protected Location[] locations;
	protected MapCursor[] cursors;
	
	// This is just there
	protected Set< BigChunkLocation > needsRender = new HashSet< BigChunkLocation >();
	
	// These should be provided
	protected Collection< WorldPixel > worldPixels;
	protected Collection< MapPixel > mapPixels;
	protected Collection< WorldCursor > worldCursors;
	protected Collection< MapCursor > mapCursors;
	
	protected PlayerSetting setting;
	protected UUID uuid;
	
	protected Minimap map;
	protected MapDataCache cache;
	
	protected SimpleImage overlayImage;
	protected SimpleImage backgroundImage;
}
