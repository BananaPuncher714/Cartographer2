package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

/**
 * Contains colors for blocks.
 * 
 * @author BananaPuncher714
 */
public class MinimapPalette {
	private int defColor;
	private Map< CrossVersionMaterial, Integer > colors = new HashMap< CrossVersionMaterial, Integer >();
	private Set< CrossVersionMaterial > transparentBlocks = new HashSet< CrossVersionMaterial >();

	/**
	 * Construct a MinimapPalette with transparent black as the default color.
	 */
	public MinimapPalette() {
		this( new Color( 0, true ) );
	}
	
	/**
	 * Construct a MinimapPalette with the provided color.
	 * 
	 * @param defaultColor
	 * Cannot be null.
	 */
	public MinimapPalette( Color defaultColor ) {
		Validate.notNull( defaultColor );
		defColor = defaultColor.getRGB();
		transparentBlocks.add( new CrossVersionMaterial( Material.AIR ) );
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
		return colors.containsKey( material );
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
		Validate.notNull( material );
		return JetpImageUtil.getBestColor( getRGB( material ) );
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
		Validate.notNull( material );
		return new Color( getRGB( material ), true );
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
		Validate.notNull( material );
		if ( transparentBlocks.contains( material ) ) {
			return defColor;
		}
		if ( colors.containsKey( material ) ) {
			return colors.get( material );
		} else {
			CrossVersionMaterial universal = new CrossVersionMaterial( material.material, -1 );
			return colors.containsKey( universal ) ? colors.get( universal ) : defColor;
		}
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
		Validate.notNull( color );
		setColor( material, color.getRGB() );
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
		Validate.notNull( material );
		colors.put( material, argb );
	}
	
	/**
	 * Set the default color.
	 * 
	 * @param color
	 * An ARGB color, cannot be null.
	 */
	public void setDefaultColor( Color color ) {
		Validate.notNull( color );
		setDefaultColor( color.getRGB() );
	}
	
	/**
	 * Get the {@link CrossVersionMaterial} that are registered.
	 * 
	 * @return
	 * A new set containing the {@link CrossVersionMaterial}.
	 */
	public Set< CrossVersionMaterial > getMaterials() {
		return new HashSet< CrossVersionMaterial >( colors.keySet() );
	}
	
	/**
	 * Get the {@link CrossVersionMaterial} that are registered as transparent.
	 * 
	 * @return
	 * The set of transparent {@link CrossVersionMaterial} for this palette.
	 */
	public Set< CrossVersionMaterial > getTransparentBlocks() {
		return transparentBlocks;
	}
	
	/**
	 * Add a {@link CrossVersionMaterial} as transparent.
	 * 
	 * @param material
	 * Cannot be null.
	 */
	public void addTransparentMaterial( CrossVersionMaterial material ) {
		Validate.notNull( material );
		transparentBlocks.add( material );
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
		return transparentBlocks.contains( material );
	}
	
	/**
	 * Set the default color.
	 * 
	 * @param argb
	 * An ARGB integer.
	 */
	public void setDefaultColor( int argb ) {
		defColor = argb;
	}
	
	/**
	 * Get the default color for this palette.
	 * 
	 * @return
	 * An ARGB color.
	 */
	public Color getDefaultColor() {
		return new Color( defColor, true );
	}
	
	/**
	 * Get the default integer color for this palette.
	 * 
	 * @return
	 * An ARGB integer.
	 */
	public int getDefaultRGB() {
		return defColor;
	}
}
