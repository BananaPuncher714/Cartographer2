package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;

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
			
			for ( Material material : template.getMaterials() ) {
				palette.setColor( material, template.getRGB( material ) );
				palette.getTransparentBlocks().remove( material );
			}
			
			for ( Material material : template.getTransparentBlocks() ) {
				palette.getTransparentBlocks().add( material );
			}
		}
		return palette;
	}
	
	
	public MinimapPalette load( FileConfiguration config ) {
		MinimapPalette palette = new MinimapPalette( new Color( 0, 0, 0, 255 ) );
		String defColor = config.getString( "default-color", "TRANSPARENT" );
		if ( defColor.equalsIgnoreCase( "TRANSPARENT" ) ) {
			palette.setDefaultColor( new Color( 0 ) );
		} else {
			String[] data = defColor.split( "\\D+" );
			Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
			palette.setDefaultColor( color );
		}
		if ( config.contains( "colors" ) ) {
			for ( String key : config.getConfigurationSection( "colors" ).getKeys( false ) ) {
				Material material = Material.getMaterial( key.toUpperCase() );
				if ( material == null ) {
					plugin.getLogger().warning( key + " is an invalid material!" );
					continue;
				}
				
				String[] data = config.getString( "colors." + key ).split( "\\D+" );
				Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
				
				palette.setColor( material, color );
			}
		}
		
		if ( config.contains( "transparent-blocks" ) ) {
			for ( String val : config.getStringList( "transparent-blocks" ) ) {
				Material material = Material.getMaterial( val.toUpperCase() );
				if ( material == null ) {
					plugin.getLogger().warning( val + " is an invalid material!" );
					continue;
				}
				palette.addTransparentMaterial( material );
			}
		}
		return palette;
	}
}
