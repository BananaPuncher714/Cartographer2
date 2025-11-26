package io.github.bananapuncher714.cartographer.core.locale;

import org.bukkit.command.CommandSender;

public abstract class LocaleMessage {
	public abstract String getMessageFor( CommandSender sender, Object... params );
}
