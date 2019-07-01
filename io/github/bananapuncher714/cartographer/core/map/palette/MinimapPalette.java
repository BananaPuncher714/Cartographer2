package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MinimapPalette {
	private int defColor;
	private Map< Material, Integer > colors = new EnumMap< Material, Integer >( Material.class );
	private Set< Material > transparentBlocks = EnumSet.noneOf( Material.class );
	
	public MinimapPalette() {
		this( new Color( 0 ) );
	}
	
	public MinimapPalette( Color defaultColor ) {
		defColor = defaultColor.getRGB();
	}
	
	public boolean contains( Material material ) {
		return colors.containsKey( material );
	}
	
	public byte getMinecraftColor( Material material ) {
		return JetpImageUtil.getBestColor( getRGB( material ) );
	}
	
	public Color getColor( Material material ) {
		return new Color( getRGB( material ) );
	}
	
	public int getRGB( Material material ) {
		return colors.containsKey( material ) ? colors.get( material ) : defColor;
	}
	
	public void setColor( Material material, Color color ) {
		setColor( material, color.getRGB() );
	}
	
	public void setColor( Material material, int rgb ) {
		colors.put( material, rgb & 0xFFFFFF );
	}
	
	public void setDefaultColor( Color color ) {
		setDefaultColor( color.getRGB() );
	}
	
	public Set< Material > getMaterials() {
		return colors.keySet();
	}
	
	public Set< Material > getTransparentBlocks() {
		return transparentBlocks;
	}
	
	public void addTransparentMaterial( Material material ) {
		transparentBlocks.add( material );
	}
	
	public boolean isTransparent( Material material ) {
		return transparentBlocks.contains( material );
	}
	
	public void setDefaultColor( int rgb ) {
		defColor = rgb;
	}
	
	public Color getDefaultColor() {
		return new Color( defColor );
	}
	
	public int getDefaultRGB() {
		return defColor;
	}
}
