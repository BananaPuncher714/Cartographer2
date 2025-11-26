package io.github.bananapuncher714.cartographer.module.vanilla.providers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.vanilla.CursorVisibility;
import io.github.bananapuncher714.cartographer.module.vanilla.NamedLocation;
import io.github.bananapuncher714.cartographer.module.vanilla.VanillaPlus;

public class CursorConverterNamedLocation implements CursorConverter {
	private VanillaPlus module;
	
	private String name;
	private boolean showName;
	private String displayName;
	private CursorVisibility visibility;
	private Type type;
	
	public CursorConverterNamedLocation( VanillaPlus module, String name ) {
		this.module = module;
		this.name = name;
	}

	@Override
	public WorldCursor convert( Object object, Player player, PlayerSetting settings ) {
		NamedLocation location = ( NamedLocation ) object;
		
		Location playerLoc = settings.getLocation();
		Location loc = location.location;
		if ( visibility != CursorVisibility.NONE && module.isWhitelisted( loc.getWorld() ) ) {
			loc = loc.clone();
			loc.setYaw( settings.isRotating() ? playerLoc.getYaw() : 180 );
			
			return new WorldCursor( showName ? displayName : null, loc, type, visibility == CursorVisibility.FULL );
		}
		
		return null;
	}

	@Override
	public boolean convertable( Object type ) {
		return type instanceof NamedLocation && ( ( NamedLocation ) type ).name.equalsIgnoreCase( name );
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isShowName() {
		return showName;
	}

	public CursorConverterNamedLocation setShowName( boolean showName ) {
		this.showName = showName;
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public CursorConverterNamedLocation setDisplayName( String displayName ) {
		this.displayName = displayName;
		return this;
	}

	public CursorVisibility getVisibility() {
		return visibility;
	}

	public CursorConverterNamedLocation setVisibility( CursorVisibility visibility ) {
		this.visibility = visibility;
		return this;
	}

	public Type getType() {
		return type;
	}

	public CursorConverterNamedLocation setType( Type type ) {
		this.type = type;
		return this;
	}

	public CursorConverterNamedLocation copyOf() {
		return new CursorConverterNamedLocation( module, name )
				.setDisplayName( displayName )
				.setShowName( showName )
				.setVisibility( visibility )
				.setType( type );
	}
}
