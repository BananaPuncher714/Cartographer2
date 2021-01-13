package io.github.bananapuncher714.cartographer.module.griefprevention;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldPixel;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class ClaimBorderShader implements WorldPixelProvider {
	private GriefPreventionModule module;
	
	public ClaimBorderShader( GriefPreventionModule module ) {
		this.module = module;
	}
	
	@Override
	public Collection< WorldPixel > getWorldPixels( Player player, Minimap map, PlayerSetting setting ) {
		Set< WorldPixel > pixels = new HashSet< WorldPixel >();

		MapViewer viewer = module.getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );
		
		
		
		return pixels;
	}

}
