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
	
	public PermissionBuilder setDescription( PermissionDefault permDefault ) {
		permission.setDefault( permDefault );
		return this;
	}
	
	public PermissionBuilder addChild( Permission permission, boolean set ) {
		permission.getChildren().put( permission.getName(), set );
		return this;
	}
	
	public PermissionBuilder register() {
		Bukkit.getPluginManager().removePermission( permission.getName() );
		Bukkit.getPluginManager().addPermission( build() );
		return this;
	}
	
	public Permission build() {
		permission.recalculatePermissibles();
		return permission;
	}
}
