package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.CursorVisibility;

public class CursorConverterEntity implements CursorConverter {
	private EntityType type;
	private Type icon;
	private CursorVisibility visibility;
	private boolean showName;
	
	public CursorConverterEntity( EntityType type) {
		this.type = type;
	}
	
	@Override
	public WorldCursor convert( Object object, Player player, PlayerSetting settings ) {
		Entity tracking = ( Entity ) object;
		String name = tracking.getCustomName();
		if ( name == null || name.isEmpty() ) {
			name = null;
		}
		if ( visibility != CursorVisibility.NONE ) {
			return new WorldCursor( showName ? name : null, tracking.getLocation(), icon, visibility == CursorVisibility.FULL );
		}
		return null;
	}

	@Override
	public boolean convertable( Object type ) {
		return type instanceof Entity && ( ( Entity ) type ).getType() == this.type;
	}
	
	public EntityType getType() {
		return type;
	}
	
	public Type getIcon() {
		return icon;
	}

	public CursorConverterEntity setIcon( Type icon ) {
		this.icon = icon;
		return this;
	}

	public CursorVisibility getVisibility() {
		return visibility;
	}

	public CursorConverterEntity setVisibility( CursorVisibility visibility ) {
		this.visibility = visibility;
		return this;
	}

	public boolean isShowName() {
		return showName;
	}

	public CursorConverterEntity setShowName( boolean showName ) {
		this.showName = showName;
		return this;
	}

	@Override
	public CursorConverterEntity copyOf() {
		return new CursorConverterEntity( type )
				.setIcon( icon )
				.setShowName( showName )
				.setVisibility( visibility );
	}
}
