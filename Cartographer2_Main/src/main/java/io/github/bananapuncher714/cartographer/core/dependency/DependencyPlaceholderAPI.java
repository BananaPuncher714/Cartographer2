package io.github.bananapuncher714.cartographer.core.dependency;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import me.clip.placeholderapi.PlaceholderAPI;

public class DependencyPlaceholderAPI {
	public String translate( CommandSender sender, String string ) {
		if ( sender instanceof OfflinePlayer ) {
			return PlaceholderAPI.setPlaceholders( ( OfflinePlayer ) sender, string );
		}
		return PlaceholderAPI.setPlaceholders( ( OfflinePlayer ) null, string );
	}
}
