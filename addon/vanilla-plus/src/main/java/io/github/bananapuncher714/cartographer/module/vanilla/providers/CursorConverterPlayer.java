package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.CursorVisibility;

public class CursorConverterPlayer implements CursorConverter {
	private UUID player;
	private Type icon;
	private CursorVisibility visibility;
	private boolean showName;
	
	public CursorConverterPlayer( UUID uuid ) {
		player = uuid;
	}
	
	@Override
	public WorldCursor convert( Object object, Player player, PlayerSetting settings ) {
		Player tracking = ( Player ) object;
		if ( visibility != CursorVisibility.NONE ) {
			return new WorldCursor( showName ? tracking.getName() : null, tracking.getLocation(), icon, visibility == CursorVisibility.FULL );
		}
		return null;
	}

	@Override
	public boolean convertable( Object type ) {
		return type instanceof Player && ( player == null || ( ( Player ) type ).getUniqueId().equals( player ) );
	}
	
	public UUID getUUID() {
		return player;
	}
	
	public CursorConverterPlayer setUUID( UUID uuid ) {
		player = uuid;
		return this;
	}
	
	public Type getIcon() {
		return icon;
	}

	public CursorConverterPlayer setIcon( Type icon ) {
		this.icon = icon;
		return this;
	}

	public CursorVisibility getVisibility() {
		return visibility;
	}

	public CursorConverterPlayer setVisibility( CursorVisibility visibility ) {
		this.visibility = visibility;
		return this;
	}

	public boolean isShowName() {
		return showName;
	}

	public CursorConverterPlayer setShowName( boolean showName ) {
		this.showName = showName;
		return this;
	}

	@Override
	public CursorConverterPlayer copyOf() {
		return new CursorConverterPlayer( player )
				.setIcon( icon )
				.setShowName( showName )
				.setVisibility( visibility );
	}
}
