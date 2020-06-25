package io.github.bananapuncher714.cartographer.core.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class Util_1_9 extends Util_1_8 {
	@Override
	public boolean isValidHand( PlayerInteractEvent event ) {
		return event.getHand() == EquipmentSlot.HAND;
	}
	
	@Override
	public ItemStack getMainHandItem( Player player ) {
		return player.getEquipment().getItemInMainHand();
	}
	
	@Override
	public ItemStack getOffHandItem( Player player ) {
		return player.getEquipment().getItemInOffHand();
	}
}
