package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * Contains colors for blocks.
 * 
 * @author BananaPuncher714
 */
public class MinimapPalette {
	/**
	 * Construct a MinimapPalette with transparent black as the default color.
	 */
	public MinimapPalette() {
	}
	
	/**
	 * Construct a MinimapPalette with the provided color.
	 * 
	 * @param defaultColor
	 * Cannot be null.
	 */
	public MinimapPalette( Color defaultColor ) {
	}
	
	/**
	 * Check if the material has a color registered.
	 * 
	 * @param material
	 * A {@link CrossVersionMaterial}.
	 * @return
	 * If the map contains the exact {@link CrossVersionMaterial} provided.
	 */
	public boolean contains( CrossVersionMaterial material ) {
		return true;
	}
	
	/**
	 * Get the Minecraft color for a particular {@link CrossVersionMaterial} as designated by this palette.
	 * 
	 * @param material
	 * Cannot be null.
	 * @return
	 * The byte representing the color as defined by MapPalette.
	 */
	public byte getMinecraftColor( CrossVersionMaterial material ) {
		return 0;
	}
	
	/**
	 * Get the color of a {@link CrossVersionMaterial} as designated by this palette.
	 * 
	 * @param material
	 * Cannot be null.
	 * @return
	 * An ARGB color.
	 */
	public Color getColor( CrossVersionMaterial material ) {
		return null;
	}
	
	/**
	 * Get the integer color of a {@link CrossVersionMaterial} as designated by this palette.
	 * 
	 * @param material
	 * Cannot be null.
	 * @return
	 * The default color if the material is listed as transparent, or if not found.
	 * Will attempt to get the color for the universal material of the {@link CrossVersionMaterial} provided if it exists. (Data value of -1)
	 */
	public int getRGB( CrossVersionMaterial material ) {
		return 0;
	}
	
	/**
	 * Set the color of a {@link CrossVersionMaterial}.
	 * 
	 * @param material
	 * Cannot be null.
	 * @param color
	 * An ARGB color, cannot be null.
	 */
	public void setColor( CrossVersionMaterial material, Color color ) {
	}
	
	/**
	 * Set the color of a {@link CrossVersionMaterial}.
	 * 
	 * @param material
	 * Cannot be null.
	 * @param argb
	 * An ARGB integer.
	 */
	public void setColor( CrossVersionMaterial material, int argb ) {
	}
	
	/**
	 * Set the default color.
	 * 
	 * @param color
	 * An ARGB color, cannot be null.
	 */
	public void setDefaultColor( Color color ) {
	}
	
	/**
	 * Get the {@link CrossVersionMaterial} that are registered.
	 * 
	 * @return
	 * A new set containing the {@link CrossVersionMaterial}.
	 */
	public Set< CrossVersionMaterial > getMaterials() {
		return null;
	}
	
	/**
	 * Get the {@link CrossVersionMaterial} that are registered as transparent.
	 * 
	 * @return
	 * The set of transparent {@link CrossVersionMaterial} for this palette.
	 */
	public Set< CrossVersionMaterial > getTransparentBlocks() {
		return null;
	}
	
	/**
	 * Add a {@link CrossVersionMaterial} as transparent.
	 * 
	 * @param material
	 * Cannot be null.
	 */
	public void addTransparentMaterial( CrossVersionMaterial material ) {
	}
	
	/**
	 * Check if the material is listed as transparent by this palette.
	 * 
	 * @param material
	 * The {@link CrossVersionMaterial} to check.
	 * @return
	 * If the material is transparent as defined by this palette.
	 */
	public boolean isTransparent( CrossVersionMaterial material ) {
		return false;
	}
	
	/**
	 * Set the default color.
	 * 
	 * @param argb
	 * An ARGB integer.
	 */
	public void setDefaultColor( int argb ) {
	}
	
	/**
	 * Get the default color for this palette.
	 * 
	 * @return
	 * An ARGB color.
	 */
	public Color getDefaultColor() {
		return null;
	}
	
	/**
	 * Get the default integer color for this palette.
	 * 
	 * @return
	 * An ARGB integer.
	 */
	public int getDefaultRGB() {
		return 0;
	}
}
