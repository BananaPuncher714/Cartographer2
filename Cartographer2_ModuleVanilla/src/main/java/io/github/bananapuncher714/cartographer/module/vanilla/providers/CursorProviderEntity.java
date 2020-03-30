package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class CursorProviderEntity implements ObjectProvider< Entity > {
	protected EntityType type;
	protected double range;
	
	public CursorProviderEntity( EntityType type, double range ) {
		this.type = type;
		this.range = range;
	}
	
	@Override
	public Set< Entity > getFor( Player player, PlayerSetting settings ) {
		return settings.getLocation().getWorld().getNearbyEntities( settings.getLocation(), range, range, range ).stream()
				.filter( ent -> ent.getType() == type )
				.collect( Collectors.toSet() );
	}
}
