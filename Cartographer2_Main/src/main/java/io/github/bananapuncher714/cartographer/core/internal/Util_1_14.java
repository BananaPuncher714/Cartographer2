package io.github.bananapuncher714.cartographer.core.internal;

import org.apache.commons.lang.Validate;
import org.bukkit.event.block.BlockPhysicsEvent;

public class Util_1_14 extends Util_1_13 {
	@Override
	public boolean updateEvent( BlockPhysicsEvent event ) {
		Validate.notNull( event );
		return event.getSourceBlock() == event.getBlock();
	}
}
