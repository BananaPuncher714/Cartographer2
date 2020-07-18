package io.github.bananapuncher714.cartographer.core.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.map.MapCursorProvider;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;

public class DefaultPointerCursorProvider implements MapCursorProvider {
	@Override
	public Collection< MapCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		Set< MapCursor > cursors = new HashSet< MapCursor >();

		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );

		if ( viewer.getSetting( MapViewer.CURSOR ) && setting.isMainHand() ) {
			Type type = FailSafe.getEnum( Type.class, "SMALL_WHITE_CIRCLE", "WHITE_CIRCLE", "WHITE_CROSS" );

			int x = ( int ) Math.max( -128, Math.min( 127, setting.getCursorX() ) );
			int y = ( int ) Math.max( -128, Math.min( 127, setting.getCursorY() ) );

			MapCursor cursor = Cartographer.getInstance().getHandler().constructMapCursor( x, y, 0, type, null );

			cursors.add( cursor );
		}

		return cursors;
	}
}
