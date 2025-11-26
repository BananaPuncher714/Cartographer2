package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

/**
 * Curse generics and type erasure. They weren't going to work anyways.
 * 
 * @author BananaPuncher714
 *
 */
public interface CursorConverter {
	WorldCursor convert( Object object, Player player, PlayerSetting settings );
	boolean convertable( Object type );
	CursorConverter copyOf();
}
