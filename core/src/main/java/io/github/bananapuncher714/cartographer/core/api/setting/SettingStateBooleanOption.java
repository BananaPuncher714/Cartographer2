package io.github.bananapuncher714.cartographer.core.api.setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;

public class SettingStateBooleanOption extends SettingState< BooleanOption > {
	private Map< String, BooleanOption > options;
	private BooleanOption defaultValue;
	
	protected SettingStateBooleanOption( String id, boolean isPrivate, BooleanOption defaultValue ) {
		super( id, isPrivate, BooleanOption.class );
		
		Map< String, BooleanOption > optionMap = new HashMap< String, BooleanOption >();
		
		optionMap.put( "default", BooleanOption.UNSET );
		optionMap.put( "unset", BooleanOption.UNSET );
		optionMap.put( "on", BooleanOption.TRUE );
		optionMap.put( "true", BooleanOption.TRUE );
		optionMap.put( "off", BooleanOption.FALSE );
		optionMap.put( "false", BooleanOption.FALSE );
		
		options = ImmutableMap.copyOf( optionMap );
		
		this.defaultValue = defaultValue;
	}

	@Override
	public String convertToString( BooleanOption value ) {
		return value.name().toLowerCase();
	}
	
	@Override
	public Optional< BooleanOption > getFrom( String value ) {
		return Optional.ofNullable( options.get( value.toLowerCase() ) );
	}

	@Override
	public Collection< String > getValues() {
		return options.keySet();
	}
	
	@Override
	public BooleanOption getDefault() {
		return defaultValue;
	}
	
	public static SettingStateBooleanOption of( String id, boolean isPrivate, BooleanOption defaultValue ) {
		return new SettingStateBooleanOption( id, isPrivate, defaultValue );
	}
}
