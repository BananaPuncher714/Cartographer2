package io.github.bananapuncher714.cartographer.module.residence;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;

public class ClaimProperty {
	protected Color color;
	protected Map< Flags, Boolean > flags = new HashMap< Flags, Boolean >();
	
	public ClaimProperty( Color color ) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Map< Flags, Boolean > getFlags() {
		return flags;
	}
	
	public boolean matches( Player player, ClaimedResidence claim ) {
		ResidencePermissions permissions = claim.getPermissions();
		for ( Entry< Flags, Boolean > entry : flags.entrySet() ) {
			if ( permissions.playerHas( player, permissions.getWorld(), entry.getKey(), false ) != entry.getValue() ) {
				return false;
			}
		}
		return true;
	}
}
