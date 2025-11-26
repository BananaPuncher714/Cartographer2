package io.github.bananapuncher714.cartographer.core.api.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PermissionBuilder {
	protected Permission permission;
	
	public PermissionBuilder( String permission ) {
		this.permission = new Permission( permission );
	}
	
	public PermissionBuilder setDescription( String description ) {
		permission.setDescription( description );
		return this;
	}
	
	public PermissionBuilder setDefault( PermissionDefault permDefault ) {
		permission.setDefault( permDefault );
		return this;
	}
	
	public PermissionBuilder addChild( Permission permission, boolean set ) {
		return addChild( permission.getName(), set );
	}
	
	public PermissionBuilder addChild( String permission, boolean set ) {
		this.permission.getChildren().put( permission, set );
		return this;
	}
	
	public PermissionBuilder register() {
		Bukkit.getPluginManager().addPermission( build() );
		return this;
	}
	
	public Permission build() {
		Bukkit.getPluginManager().removePermission( permission.getName() );
		permission.recalculatePermissibles();
		return permission;
	}
}
