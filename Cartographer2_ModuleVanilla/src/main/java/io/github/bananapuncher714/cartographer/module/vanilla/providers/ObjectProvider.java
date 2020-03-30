package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import java.util.Set;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * No point to rely on generics when you have type erasure.
 * 
 * @author BananaPuncher714
 */
public interface ObjectProvider< T > {
	Set< T > getFor( Player player, PlayerSetting settings );
}
