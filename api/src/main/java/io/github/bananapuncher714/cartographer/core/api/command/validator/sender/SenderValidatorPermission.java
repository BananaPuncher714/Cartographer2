package io.github.bananapuncher714.cartographer.core.api.command.validator.sender;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

public class SenderValidatorPermission implements SenderValidator {
	protected Set< String > permissions = new HashSet< String >();
	
	public SenderValidatorPermission( String... permissions ) {
		for ( String permission : permissions  ) {
			this.permissions.add( permission );
		}
	}
	
	@Override
	public boolean isValid( CommandSender sender ) {
		for ( String permission : permissions ) {
			if ( sender.hasPermission( permission ) ) {
				return true;
			}
		}
		return false;
	}

}
