package io.github.bananapuncher714.cartographer.module.worldguard;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.worldguard.api.CuboidRegion;
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
			for ( CuboidRegion region : wrapper.getRegionsFor( playerLoc.getWorld() ) ) {
				String name = region.getName();

				RegionColors colors = module.getFor( name );
				Color color = colors.getNonMember();
				if ( region.getOwners().contains( uuid ) ) {
					color = colors.getOwner();
				} else if ( region.getMembers().contains( uuid ) ) {
					color = colors.getMember();
				}

				Location min = region.getMin();
				Location max = region.getMax();
				double width = max.getX() - min.getX();
				double height = max.getZ() - min.getZ();

				int pixelWidth = setting.getScale() < 1 ? 2 : 1;
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
			}
		}

		return pixels;
	}

}
