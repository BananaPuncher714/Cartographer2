package io.github.bananapuncher714.cartographer.module.residence;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class ClaimBorderShader implements WorldPixelProvider{
	protected ResidenceModule module;
	
	public ClaimBorderShader( ResidenceModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldPixel > getWorldPixels( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldPixel > pixels = new HashSet< WorldPixel >();
		
		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		if ( viewer.getSetting( ResidenceModule.RESIDENCE_CLAIMS ) ) {
			Map< String, ClaimedResidence > residences = Residence.getInstance().getResidenceManager().getResidences();
			for ( Entry< String, ClaimedResidence > entry : residences.entrySet() ) {
				ClaimedResidence residence = entry.getValue();
				
				Optional< Color > optionalColor = Optional.empty();
				if ( residence.getOwnerUUID().equals( player.getUniqueId() ) ) {
					optionalColor = module.getOwnerColor();
				} else {
					Optional< ClaimProperty > property = module.getMatching( player, residence );
					if ( property.isPresent() ) {
						optionalColor = Optional.of( property.get().getColor() );
					}
				}
				
				if ( optionalColor.isPresent() ) {
					Color color = optionalColor.get();
					
					for ( CuboidArea area : residence.getAreaArray() ) {
						int pixelWidth = setting.getScale() < 1 ? 2 : 1;
						Location min = area.getLowLoc();
						double thickness = Math.min( setting.getScale(), area.getXSize() ) * pixelWidth;
						double thicknessHeight = Math.min( setting.getScale(), area.getZSize() ) * pixelWidth;
						
						WorldPixel north = new WorldPixel( area.getWorld(), min.getX(), min.getZ(), color );
						north.setWidth( area.getXSize() );
						north.setHeight( thicknessHeight );
						pixels.add( north );
						
						WorldPixel south = new WorldPixel( area.getWorld(), min.getX(), min.getZ() + area.getZSize() - thicknessHeight, color );
						south.setWidth( area.getXSize() );
						south.setHeight( thicknessHeight );
						pixels.add( south );
						
						WorldPixel west = new WorldPixel( area.getWorld(), min.getX(), min.getZ(), color );
						west.setWidth( thickness );
						west.setHeight( area.getZSize() );
						pixels.add( west );
						
						WorldPixel east = new WorldPixel( area.getWorld(), min.getX() + area.getXSize() - thickness, min.getZ(), color );
						east.setWidth( thickness );
						east.setHeight( area.getZSize() );
						pixels.add( east );
					}
				}
			}
		}
		return pixels;
	}
}
