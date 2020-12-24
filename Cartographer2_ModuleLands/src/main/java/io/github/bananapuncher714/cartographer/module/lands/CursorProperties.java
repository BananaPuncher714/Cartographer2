package io.github.bananapuncher714.cartographer.module.lands;

import java.util.function.Supplier;

import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.module.lands.visibility.CursorVisibility;

public class CursorProperties {
	protected boolean enabled = true;
	protected Type type;
	protected double radius = 0;
	protected CursorVisibility visibility = CursorVisibility.PARTIAL;
	protected Supplier< String > nameSupplier;
	protected boolean showName;
	
	public CursorProperties( Type type, Supplier< String > nameSupplier ) {
		this.type = type;
		this.nameSupplier = nameSupplier;
		showName = nameSupplier != null;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public CursorProperties setEnabled( boolean enabled ) {
		this.enabled = enabled;
		return this;
	}

	public Type getType() {
		return type;
	}

	public CursorProperties setType( Type type ) {
		this.type = type;
		return this;
	}

	public double getRadius() {
		return radius;
	}

	public CursorProperties setRadius( double radius ) {
		this.radius = radius;
		return this;
	}

	public CursorVisibility getVisibility() {
		return visibility;
	}

	public CursorProperties setVisibility( CursorVisibility visibility ) {
		this.visibility = visibility;
		return this;
	}

	public Supplier< String > getNameSupplier() {
		return nameSupplier;
	}
	
	public String getName() {
		if ( nameSupplier != null ) {
			return nameSupplier.get();
		}
		return null;
	}

	public CursorProperties setNameSupplier( Supplier< String > nameSupplier ) {
		this.nameSupplier = nameSupplier;
		return this;
	}

	public boolean isShowName() {
		return showName;
	}

	public CursorProperties setShowName( boolean showName ) {
		this.showName = showName;
		return this;
	}
}
