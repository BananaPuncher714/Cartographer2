package io.github.bananapuncher714.cartographer.core.internal;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class Util_1_14 extends Util_1_13 {
	public Util_1_14() {
		super( true );
	}
	
	@Override
	public MapView getMapViewFrom( ItemStack item ) {
		if ( item == null ) {
			return null;
		}
		ItemMeta meta = item.getItemMeta();
		if ( meta instanceof MapMeta ) {
			return getMap( ( ( MapMeta ) meta ).getMapId() );
		}
		return null;
	}
	
	@Override
	public MapView getMap( int id ) {
		return Bukkit.getMap( id );
	}
	
	@Override
	public boolean updateEvent( BlockPhysicsEvent event ) {
		Validate.notNull( event );
		return event.getSourceBlock() == event.getBlock();
	}
}
