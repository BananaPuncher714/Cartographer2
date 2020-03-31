package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class CursorProviderPlayer implements ObjectProvider< Player > {
	private final double rangeSquared;
	
	public CursorProviderPlayer( double range ) {
		rangeSquared = range * range;
	}
	
	@Override
	public Set< Player > getFor( Player player, PlayerSetting settings ) {
		return settings.getLocation().getWorld().getPlayers().stream()
				.filter( pl -> pl.getLocation().distanceSquared( settings.getLocation() ) <= rangeSquared )
				.filter( pl -> !pl.hasPotionEffect( PotionEffectType.INVISIBILITY ) )
				.filter( pl -> !pl.hasPermission( "vanillaplus.invisible" ) )
				.filter( pl -> !pl.isSneaking() )
				.filter( pl -> pl != player )
				.collect( Collectors.toSet() );
	}
}
