package io.github.bananapuncher714.cartographer.module.lands.settings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.module.lands.visibility.LandVisibility;

public class SettingStateLandVisibility extends SettingState< LandVisibility > {
	protected final Map< String, LandVisibility > values = new HashMap< String, LandVisibility >();
	protected LandVisibility defaultVisibility;
	
	public SettingStateLandVisibility( String id, boolean isPrivate, LandVisibility defVis, LandVisibility... values ) {
		super( id, isPrivate, LandVisibility.class );
		
		this.defaultVisibility = defVis;
		for ( LandVisibility vis : values ) {
			this.values.put( vis.name(), vis );
		}
	}
	
	@Override
	public String convertToString( LandVisibility value ) {
		return value.name();
	}

	@Override
	public Optional< LandVisibility > getFrom( String value ) {
		return Optional.ofNullable( values.get( value ) );
	}

	@Override
	public Collection< String > getValues() {
		return values.keySet();
	}

	@Override
	public LandVisibility getDefault() {
		return defaultVisibility;
	}
}
