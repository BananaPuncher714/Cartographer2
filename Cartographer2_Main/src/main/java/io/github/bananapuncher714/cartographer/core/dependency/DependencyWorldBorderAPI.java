package io.github.bananapuncher714.cartographer.core.dependency;

import org.bukkit.Location;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

public class DependencyWorldBorderAPI {
	public boolean shouldLocationBeLoaded( Location location ) {
		BorderData data = Config.Border( location.getWorld().getName() );
		if ( data != null ) {
			return data.insideBorder( location );
		}
		return true;
	}
}
