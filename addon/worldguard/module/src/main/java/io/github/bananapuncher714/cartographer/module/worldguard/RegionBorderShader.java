package io.github.bananapuncher714.cartographer.module.worldguard;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.worldguard.api.CuboidRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.PolygonalRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardRegion;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardWrapper;

public class RegionBorderShader implements WorldPixelProvider {
	protected WorldGuardModule module;

	public RegionBorderShader( WorldGuardModule module ) {
		this.module = module;
	}

	@Override
	public Collection< WorldPixel > getWorldPixels( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldPixel > pixels = new HashSet< WorldPixel >();

		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		if ( viewer.getSetting( WorldGuardModule.WORLDGUARD_REGIONS ) ) {
			Location playerLoc = setting.getLocation();
			UUID uuid = setting.getUUID();
			WorldGuardWrapper wrapper = module.getWrapper();
			for ( WorldGuardRegion region : wrapper.getRegionsFor( playerLoc.getWorld() ) ) {
				String name = region.getName();

				RegionColors colors = module.getFor( name );
				Color color = colors.getNonMember();
				if ( region.getOwners().contains( uuid ) ) {
					color = colors.getOwner();
				} else if ( region.getMembers().contains( uuid ) ) {
					color = colors.getMember();
				}

				int pixelWidth = setting.getScale() < 1 ? 2 : 1;
				if ( region instanceof CuboidRegion ) {
					CuboidRegion cuboid = ( CuboidRegion ) region;
					
					Location min = cuboid.getMin();
					Location max = cuboid.getMax();
					double width = max.getX() - min.getX();
					double height = max.getZ() - min.getZ();
	
					double thickness = Math.min( setting.getScale(), width ) * pixelWidth;
					double thicknessHeight = Math.min( setting.getScale(), height ) * pixelWidth;
	
					WorldPixel north = new WorldPixel( playerLoc.getWorld(), min.getX(), min.getZ(), color );
					north.setWidth( width );
					north.setHeight( thicknessHeight );
					pixels.add( north );
	
					WorldPixel south = new WorldPixel( playerLoc.getWorld(), min.getX(), max.getZ() - thicknessHeight, color );
					south.setWidth( width );
					south.setHeight( thicknessHeight );
					pixels.add( south );
	
					WorldPixel west = new WorldPixel( playerLoc.getWorld(), min.getX(), min.getZ(), color );
					west.setWidth( thickness );
					west.setHeight( height );
					pixels.add( west );
	
					WorldPixel east = new WorldPixel( playerLoc.getWorld(), max.getX() - thickness, min.getZ(), color );
					east.setWidth( thickness );
					east.setHeight( height );
					pixels.add( east );
				} else if ( region instanceof PolygonalRegion ) {
					PolygonalRegion polygon = ( PolygonalRegion ) region;
					
					// We need to create a bunch of WorldPixels
					for ( int i = 0; i < polygon.getLocations().size(); i++ ) {
						Location loc1 = polygon.getLocations().get( i );
						Location loc2 = polygon.getLocations().get( ( i + 1 ) % polygon.getLocations().size() );
						loc1.setY( loc2.getY() );
						
						// So, we have the two locations that we need to get a point between
						// First, check if it's a straight line, so that we can create less pixels
						Vector to = loc1.clone().subtract( loc2 ).toVector().normalize();
						double totalWidth = setting.getScale() * pixelWidth;
						double halfWidth = totalWidth / 2.0;
						Location start = loc1.clone().add( .5 - halfWidth, 0, .5 - halfWidth );
						Location end = loc2.clone().add( .5 - halfWidth, 0, .5 - halfWidth );
						// Place startX and startY in the center of the block
						double startX = Math.min( start.getX(), end.getX() );
						double startY = Math.min( start.getZ(), end.getZ() );
						// Place the endX and endY there too
						double endX = Math.max( start.getX(), end.getX() );
						double endY = Math.max( start.getZ(), end.getZ() );
						if ( Math.abs( to.getX() ) == 1 ) {
							// It's horizontal
							// It goes from left to right
							WorldPixel pixel = new WorldPixel( playerLoc.getWorld(), startX, startY, color );
							// Apparently the horizontal line doesn't quite connect properly...
							// So just add a little bit more to the end
							// It still doesn't completely fit, but oh well
							pixel.setWidth( ( endX - startX ) + Math.min( 1, setting.getScale() ) );
							pixel.setHeight( totalWidth );
							pixels.add( pixel );
						} else if ( Math.abs( to.getZ() ) == 1 ) {
							// It's vertical
							WorldPixel pixel = new WorldPixel( playerLoc.getWorld(), startX, startY, color );
							pixel.setWidth( totalWidth );
							pixel.setHeight( ( endY - startY ) + Math.min( 1, setting.getScale() ) );
							pixels.add( pixel );
						} else {
							// It's not a vertical or horizontal line and we need to rasterize it
							double xDiff = start.getX() - end.getX();
							double zDiff = start.getZ() - end.getZ();
							double length = Math.max( Math.abs( xDiff ), Math.abs( zDiff ) );
							// Get the total amount of pixels required
							double pixelCount = Math.ceil( length / setting.getScale() );
							boolean horizontal = Math.abs( xDiff ) > Math.abs( zDiff );
							for ( double pixelIndex = 0; pixelIndex < pixelCount ; pixelIndex++ ) {
								double xVal = end.getX() + ( pixelIndex / pixelCount ) * xDiff;
								double yVal = end.getZ() + ( pixelIndex / pixelCount ) * zDiff;
								WorldPixel pixel = new WorldPixel( playerLoc.getWorld(), xVal, yVal, color );
								pixel.setWidth( horizontal ? setting.getScale() : totalWidth );
								pixel.setHeight( horizontal ? totalWidth : setting.getScale() );
								pixels.add( pixel );
							}
						}
					}
				}
			}
		}

		return pixels;
	}

}
