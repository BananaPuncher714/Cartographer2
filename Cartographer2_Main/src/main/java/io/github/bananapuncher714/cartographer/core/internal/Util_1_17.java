package io.github.bananapuncher714.cartographer.core.internal;

import org.bukkit.World;

public class Util_1_17 extends Util_1_14 {
	@Override
	public int getMinWorldHeight( World world ) {
		return world.getMinHeight();
	}
}
