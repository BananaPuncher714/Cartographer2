package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.Arrays;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.map.menu.Frame;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;

public class MenuComponentSolid implements MenuComponent {
	Frame frame;
	
	public MenuComponentSolid( int color ) {
		color |= 0xFF000000;
		int[] display = new int[ 128 * 128 ];
		Arrays.fill( display, color );
		frame = new Frame( display, 128 );
	}
	
	@Override
	public Frame getFrame() {
		return frame;
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public boolean onView( Player player, double x, double y ) {
		return false;
	}

	@Override
	public boolean onInteract( Player player, double x, double y, MapInteraction interaction ) {
		return interaction == MapInteraction.CTRLQ;
	}
}
