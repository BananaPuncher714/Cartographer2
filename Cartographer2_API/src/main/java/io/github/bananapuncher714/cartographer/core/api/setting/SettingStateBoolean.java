package io.github.bananapuncher714.cartographer.core.api.setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

public class SettingStateBoolean extends SettingState< Boolean > {
	private Map< String, Boolean > options;
	private boolean defaultValue;
	
	protected SettingStateBoolean( String id, boolean isPrivate, boolean defaultValue ) {
		super( id, isPrivate, Boolean.class );
		
		Map< String, Boolean > optionMap = new HashMap< String, Boolean >();
		
		optionMap.put( "on", true );
		optionMap.put( "true", true );
		optionMap.put( "off", false );
		optionMap.put( "false", false );
		
		options = ImmutableMap.copyOf( optionMap );
		
		this.defaultValue = defaultValue;
	}

	@Override
	public String convertToString( Boolean value ) {
		return value.toString();
	}
	
	@Override
	public Optional< Boolean > getFrom( String value ) {
		return Optional.ofNullable( options.get( value.toLowerCase() ) );
	}

	@Override
	public Collection< String > getValues() {
		return options.keySet();
	}
	
	@Override
	public Boolean getDefault() {
		return defaultValue;
	}
	
	public static SettingStateBoolean of( String id, boolean isPrivate, boolean defaultValue ) {
		return new SettingStateBoolean( id, isPrivate, defaultValue );
	}
}
