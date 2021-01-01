package io.github.bananapuncher714.cartographer.core;

import java.awt.Image;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.inventory.InventoryType;

import io.github.bananapuncher714.cartographer.core.api.SimpleImage;

public class CartographerSettings {
	// Blacklist of map ids to NOT use
	private Set< Integer > invalidIds = new HashSet< Integer >();
	// Inventories that minimaps cannot be put into
	private Set< InventoryType > invalidInventoryTypes = new HashSet< InventoryType >();
	
	private int chunkUpdateDelay = 10;
	// How long in ticks to update blocks on the map
	private int blockUpdateDelay = 10;
	// How many blocks to update per update tick
	private int blockUpdateAmount = 20;
	// How long in ticks until the map can be updated again
	private int renderDelay;
	// How many blocks can be updated at most per tick

	// Global default for rotation setting
	private boolean rotateByDefault = true;
	// Print out debug information regarding missing colors and materials in the console
	private boolean paletteDebug;
	// Catch the drop item packet
	private boolean preventDrop = true;
	private boolean packetDrop = true;
	// Dither the missing map image
	private boolean ditherMissing = false;
	
	private SimpleImage loadingBackground;
	private SimpleImage overlay;
	private SimpleImage missingMapImage;
	private SimpleImage disabledMap;
	
	protected Set< Integer > getInvalidIds() {
		return invalidIds;
	}
	
	public int getRenderDelay() {
		return renderDelay;
	}
	
	public void setRenderDelay( int renderDelay ) {
		this.renderDelay = renderDelay;
	}
	
	public boolean isRotateByDefault() {
		return rotateByDefault;
	}
	
	public void setRotateByDefault( boolean rotate ) {
		rotateByDefault = rotate;
	}
	
	public boolean isPaletteDebug() {
		return paletteDebug;
	}
	
	public void setPaletteDebug( boolean debug ) {
		paletteDebug = debug;
	}
	
	public boolean isPreventDrop() {
		return preventDrop;
	}
	
	public void setPreventDrop( boolean drop ) {
		preventDrop = drop;
	}
	
	public boolean isUseDropPacket() {
		return packetDrop;
	}
	
	public void setUseDropPacket( boolean packet ) {
		packetDrop = packet;
	}
	
	public boolean isDitherMissingMapImage() {
		return ditherMissing;
	}
	
	public void setDitherMissingMapImage( boolean dither ) {
		ditherMissing = dither;
	}
	
	public int getChunkUpdateDelay() {
		return chunkUpdateDelay;
	}
	
	public void setChunkUpdateDelay( int delay ) {
		chunkUpdateDelay = delay;
	}
	
	public int getBlockUpdateDelay() {
		return blockUpdateDelay;
	}
	
	public void setBlockUpdateDelay( int delay ) {
		blockUpdateDelay = delay;
	}
	
	public int getBlockUpdateAmount() {
		return blockUpdateAmount;
	}
	
	public void setBlockUpdateAmount( int amount ) {
		blockUpdateAmount = amount;
	}
	
	public SimpleImage getBackground() {
		// TODO Specify that this is 128x128
		return loadingBackground;
	}
	
	public void setBackground( SimpleImage image ) {
		loadingBackground = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
	}
	
	public SimpleImage getOverlay() {
		// TODO Specify that this is 128x128
		return overlay;
	}

	public void setOverlay( SimpleImage image ) {
		overlay = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
	}
	
	public SimpleImage getMissingMapImage() {
		return missingMapImage;
	}
	
	public void setMissingMapImage( SimpleImage image ) {
		missingMapImage = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
	}
	
	public SimpleImage getDisabledMapImage() {
		return disabledMap;
	}
	
	public void setDisabledMapImage( SimpleImage image ) {
		disabledMap = new SimpleImage( image, 128, 128, Image.SCALE_REPLICATE );
	}
	
	public boolean isValidInventory( InventoryType type ) {
		return !invalidInventoryTypes.contains( type );
	}
	
	public Set< InventoryType > getInvalidInventoryTypes() {
		return invalidInventoryTypes;
	}
}
