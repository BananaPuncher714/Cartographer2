package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.Collection;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;

public class RenderInfo {
	protected Location playerLoc;
	protected Collection< WorldPixel > pixels;
	protected byte[] data;
	protected int[] upperPixelInfo;
	protected int[] lowerPixelInfo;
	protected int[] globalOverlay;
	protected int[] background;
	protected Location[] locations;
	protected MapDataCache cache;
}
