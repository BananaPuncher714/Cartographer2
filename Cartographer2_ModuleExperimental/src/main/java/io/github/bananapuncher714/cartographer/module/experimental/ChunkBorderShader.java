package io.github.bananapuncher714.cartographer.module.experimental;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class ChunkBorderShader implements WorldPixelProvider {
	private ChunkBorderDataSupplier supplier;
	
	public ChunkBorderShader( ChunkBorderDataSupplier supplier ) {
		this.supplier = supplier;
	}
	
	@Override
	public Collection< WorldPixel > getWorldPixels( Player player, Minimap map, PlayerSetting setting ) {
		Collection< WorldPixel > pixels = new ArrayList< WorldPixel >();

		// The width of the border, in pixels. Make it 2 if it's zoomed in, or else 1
		int pixelWidth = setting.getScale() < 1 ? 2 : 1;
		// Don't want it to overflow onto other chunks if the map is zoomed out sufficiently
		double thickness = Math.min( setting.getScale(), 16 ) * pixelWidth;
		
		for ( ChunkBorderData data : supplier.getData( player, setting ) ) {
			ChunkLocation c = data.getLocation();
			Set< BlockFace > faces = data.getFaces();
			if ( faces.isEmpty() ) {
				continue;
			}
			Color color = data.getColor();
			int minX = c.getX() << 4;
			int minZ = c.getZ() << 4;
			
			if ( faces.contains( BlockFace.NORTH ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX, minZ + 16 - thickness, color );
				pixel.setWidth( 16 );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.EAST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX + 16 - thickness, minZ, color );
				pixel.setWidth( thickness );
				pixel.setHeight( 16 );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.SOUTH ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX, minZ, color );
				pixel.setWidth( 16 );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.WEST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX, minZ, color );
				pixel.setWidth( thickness );
				pixel.setHeight( 16 );
				pixels.add( pixel );
			}
			
			if ( faces.contains( BlockFace.NORTH_EAST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX + 16 - thickness, minZ + 16 - thickness, color );
				pixel.setWidth( thickness );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.NORTH_WEST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX, minZ + 16 - thickness, color );
				pixel.setWidth( thickness );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.SOUTH_EAST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX + 16 - thickness, minZ, color );
				pixel.setWidth( thickness );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
			if ( faces.contains( BlockFace.SOUTH_WEST ) ) {
				WorldPixel pixel = new WorldPixel( c.getWorld(), minX, minZ, color );
				pixel.setWidth( thickness );
				pixel.setHeight( thickness );
				pixels.add( pixel );
			}
		}
		
		return pixels;
	}
	
	public static Map< ChunkLocation, Set< BlockFace > > getBorders( Collection< ChunkLocation > locations ) {
		Map< ChunkLocation, Set< BlockFace > > borders = new HashMap< ChunkLocation, Set< BlockFace > >();
		
		for ( int i = 0; i < 2; i++) {
			Deque< ChunkLocation > remaining = new ArrayDeque< ChunkLocation >( locations );
			while ( !remaining.isEmpty() ) {
				ChunkLocation first = remaining.poll();
				Set< BlockFace > directions = new HashSet< BlockFace >();
				directions.add( BlockFace.NORTH );
				directions.add( BlockFace.EAST );
				directions.add( BlockFace.SOUTH );
				directions.add( BlockFace.WEST );
				borders.put( first, directions );
	
				// Check cardinal directions
				// Then, if 2 adjacent cardinal directions exist, see if the diagonal exists
				// 4 checks for the first 4 directions
				// Then, 4 checks for the next diagonals
				// Absolutely terrible design
				// But theres not really much else that can be done efficiently
				ChunkLocation cN = new ChunkLocation( first ).add( 0, 1 );
				ChunkLocation cE = new ChunkLocation( first ).add( 1, 0 );
				ChunkLocation cS = new ChunkLocation( first ).add( 0, -1 );
				ChunkLocation cW = new ChunkLocation( first ).add( -1, 0 );
				ChunkLocation cNW = new ChunkLocation( first ).add( -1, 1 );
				ChunkLocation cNE = new ChunkLocation( first ).add( 1, 1 );
				ChunkLocation cSW = new ChunkLocation( first ).add( -1, -1 );
				ChunkLocation cSE = new ChunkLocation( first ).add( 1, -1 );
	
				boolean hasN = borders.containsKey( cN );
				boolean hasE = borders.containsKey( cE );
				boolean hasS = borders.containsKey( cS );
				boolean hasW = borders.containsKey( cW );
	
				// Erase the borders
				if ( hasN ) {
					directions.remove( BlockFace.NORTH );
					borders.get( cN ).remove( BlockFace.SOUTH );
				}
				if ( hasE ) {
					directions.remove( BlockFace.EAST );
					borders.get( cE ).remove( BlockFace.WEST );
				}
				if ( hasS ) {
					directions.remove( BlockFace.SOUTH );
					borders.get( cS ).remove( BlockFace.NORTH );
				}
				if ( hasW ) {
					directions.remove( BlockFace.WEST );
					borders.get( cW ).remove( BlockFace.EAST );
				}
	
				// Check the corners
				if ( hasN && hasE ) {
					if ( borders.containsKey( cNE ) ) {
						directions.remove( BlockFace.NORTH_EAST );
						borders.get( cNE ).remove( BlockFace.SOUTH_WEST );
					} else {
						directions.add( BlockFace.NORTH_EAST );
					}
				}
	
				if ( hasE && hasS ) {
					if ( borders.containsKey( cSE ) ) {
						directions.remove( BlockFace.SOUTH_EAST );
						borders.get( cSE ).remove( BlockFace.NORTH_WEST );
					} else {
						directions.add( BlockFace.SOUTH_EAST );
					}
				}
				if ( hasS && hasW ) {
					if ( borders.containsKey( cSW ) ) {
						directions.remove( BlockFace.SOUTH_WEST );
						borders.get( cSW ).remove( BlockFace.NORTH_EAST );
					} else {
						directions.add( BlockFace.SOUTH_WEST );
					}
				}
				if ( hasW && hasN ) {
					if ( borders.containsKey( cNW ) ) {
						directions.remove( BlockFace.NORTH_WEST );
						borders.get( cNW ).remove( BlockFace.SOUTH_EAST );
					} else {
						directions.add( BlockFace.NORTH_WEST );
					}
				}
			}
		}
		
		return borders;
	}
	
	public static class ChunkBorderData {
		protected ChunkLocation location;
		protected Color color;
		protected Set< BlockFace > faces = new HashSet< BlockFace >();

		public ChunkBorderData( ChunkLocation location, Color color ) {
			this.location = location;
			this.color = color;
		}

		public Color getColor() {
			return color;
		}

		public void setColor( Color color ) {
			this.color = color;
		}

		public ChunkLocation getLocation() {
			return location;
		}

		public Set< BlockFace > getFaces() {
			return faces;
		}
	}
	
	public interface ChunkBorderDataSupplier {
		Collection< ChunkBorderData > getData( Player player, PlayerSetting setting );
	}
}
