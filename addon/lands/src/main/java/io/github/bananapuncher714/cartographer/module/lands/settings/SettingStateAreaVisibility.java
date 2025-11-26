package io.github.bananapuncher714.cartographer.module.lands.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.module.lands.visibility.AreaVisibility;

// Should probably make a setting that accepts a generic enum...
public class SettingStateAreaVisibility extends SettingState< AreaVisibility > {
	protected static final Map< String, AreaVisibility > VALUES;
	
	protected AreaVisibility defaultVisibility;
	
	public SettingStateAreaVisibility( String id, boolean isPrivate, AreaVisibility defVis ) {
		super( id, isPrivate, AreaVisibility.class );
		
		this.defaultVisibility = defVis;
	}
	
	static {
		Map< String, AreaVisibility > visibilities = new HashMap< String, AreaVisibility >();
		for ( AreaVisibility vis : AreaVisibility.values() ) {
			visibilities.put( vis.name(), vis );
		}
		VALUES = Collections.unmodifiableMap( visibilities );
	}
	
	@Override
	public String convertToString( AreaVisibility value ) {
		return value.name();
	}

	@Override
	public Optional< AreaVisibility > getFrom( String value ) {
		return Optional.ofNullable( VALUES.get( value ) );
	}

	@Override
	public Collection< String > getValues() {
		return VALUES.keySet();
	}

	@Override
	public AreaVisibility getDefault() {
		return defaultVisibility;
	}
}
