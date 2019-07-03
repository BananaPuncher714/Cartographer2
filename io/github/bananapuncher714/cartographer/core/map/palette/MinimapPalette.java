package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MinimapPalette {
	private int defColor;
	private Map< CrossVersionMaterial, Integer > colors = new HashMap< CrossVersionMaterial, Integer >();
	private Set< CrossVersionMaterial > transparentBlocks = new HashSet< CrossVersionMaterial >();
	
	public MinimapPalette() {
		this( new Color( 0 ) );
		transparentBlocks.add( new CrossVersionMaterial( Material.AIR ) );
	}
	
	public MinimapPalette( Color defaultColor ) {
		defColor = defaultColor.getRGB();
	}
	
	public boolean contains( CrossVersionMaterial material ) {
		return colors.containsKey( material );
	}
	
	public byte getMinecraftColor( CrossVersionMaterial material ) {
		return JetpImageUtil.getBestColor( getRGB( material ) );
	}
	
	public Color getColor( CrossVersionMaterial material ) {
		return new Color( getRGB( material ) );
	}
	
	public int getRGB( CrossVersionMaterial material ) {
		if ( colors.containsKey( material ) ) {
			return colors.get( material );
		} else {
			CrossVersionMaterial universal = new CrossVersionMaterial( material.material, -1 );
			return colors.containsKey( universal ) ? colors.get( universal ) : defColor;
		}
	}
	
	public void setColor( CrossVersionMaterial material, Color color ) {
		setColor( material, color.getRGB() );
	}
	
	public void setColor( CrossVersionMaterial material, int rgb ) {
		colors.put( material, rgb & 0xFFFFFF );
	}
	
	public void setDefaultColor( Color color ) {
		setDefaultColor( color.getRGB() );
	}
	
	public Set< CrossVersionMaterial > getMaterials() {
		return colors.keySet();
	}
	
	public Set< CrossVersionMaterial > getTransparentBlocks() {
		return transparentBlocks;
	}
	
	public void addTransparentMaterial( CrossVersionMaterial material ) {
		transparentBlocks.add( material );
	}
	
	public boolean isTransparent( CrossVersionMaterial material ) {
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
