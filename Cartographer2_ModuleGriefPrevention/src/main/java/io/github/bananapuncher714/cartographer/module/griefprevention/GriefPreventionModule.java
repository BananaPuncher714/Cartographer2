package io.github.bananapuncher714.cartographer.module.griefprevention;

import java.util.Collection;

import io.github.bananapuncher714.cartographer.core.module.Module;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionModule extends Module {

	@Override
	public void onEnable() {
		Collection< Claim > claims = GriefPrevention.instance.dataStore.getClaims();
		for ( Claim claim : claims ) {
			
		}
	}

}
