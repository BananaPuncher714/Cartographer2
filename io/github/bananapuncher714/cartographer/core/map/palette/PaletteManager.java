package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

public class PaletteManager {
	protected Cartographer plugin;
	
	protected Map< String, MinimapPalette > palettes = new HashMap< String, MinimapPalette >();
	
	public PaletteManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	public void register( String name, MinimapPalette palette ) {
		palettes.put( name, palette );
	}
	
	public MinimapPalette construct( List< String > palettes ) {
		MinimapPalette palette = new MinimapPalette();
		for ( String id : palettes ) {
			MinimapPalette template = this.palettes.get( id );
			if ( template == null ) {
				plugin.getLogger().warning( id + " palette not found!" );
				continue;
			}
		
			palette.setDefaultColor( template.getDefaultRGB() );
			
			for ( CrossVersionMaterial material : template.getMaterials() ) {
				palette.setColor( material, template.getRGB( material ) );
				palette.getTransparentBlocks().remove( material );
			}
			
			for ( CrossVersionMaterial material : template.getTransparentBlocks() ) {
				palette.getTransparentBlocks().add( material );
			}
		}
		return palette;
	}
	
	
	public MinimapPalette load( FileConfiguration config ) {
		MinimapPalette palette = new MinimapPalette( new Color( 0, 0, 0, 255 ) );
		String defColor = config.getString( "default-color", "TRANSPARENT" );
		if ( defColor.equalsIgnoreCase( "TRANSPARENT" ) ) {
			palette.setDefaultColor( new Color( 255, 0, 0, 0 ) );
		} else {
			String[] data = defColor.split( "\\D+" );
			Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
			palette.setDefaultColor( color );
		}
		if ( config.contains( "colors" ) ) {
			for ( String key : config.getConfigurationSection( "colors" ).getKeys( false ) ) {
				String[] matVals = key.split( "," );
				Material material = Material.getMaterial( matVals[ 0 ].toUpperCase() );
				int durability = matVals.length > 1 ? Integer.parseInt( matVals[ 1 ] ): 0;
				if ( material == null ) {
					plugin.getLogger().warning( key + " is an invalid material!" );
					continue;
				}
				CrossVersionMaterial cvMaterial = new CrossVersionMaterial( material, durability );
				
				String[] data = config.getString( "colors." + key ).split( "\\D+" );
				Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
				
				palette.setColor( cvMaterial, color );
			}
		}
		
		if ( config.contains( "transparent-blocks" ) ) {
			for ( String val : config.getStringList( "transparent-blocks" ) ) {
				String[] matVals = val.split( "," );
				Material material = Material.getMaterial( matVals[ 0 ].toUpperCase() );
				int durability = matVals.length > 1 ? Integer.parseInt( matVals[ 1 ] ): 0;
				if ( material == null ) {
					plugin.getLogger().warning( val + " is an invalid material!" );
					continue;
				}
				CrossVersionMaterial cvMaterial = new CrossVersionMaterial( material, durability );
				palette.addTransparentMaterial( cvMaterial );
			}
		}
		return palette;
	}
}
