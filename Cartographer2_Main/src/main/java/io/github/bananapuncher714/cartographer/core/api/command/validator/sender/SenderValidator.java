package io.github.bananapuncher714.cartographer.core.api.command.validator.sender;

import org.bukkit.command.CommandSender;

public interface SenderValidator {
	boolean isValid( CommandSender sender );
}
