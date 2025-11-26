package io.github.bananapuncher714.cartographer.core.api.setting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SettingStateEnum< T extends Enum< T > > extends SettingState< T > {
	private final Map< String, T > values;
	private final T defaultValue;
	
	@SafeVarargs
	private SettingStateEnum( String id, boolean isPrivate, Class< T > clazz, T defaultValue, T... possibleValues ) {
		super( id, isPrivate, clazz );
		
		this.defaultValue = defaultValue;
		Map< String, T > values = new HashMap< String, T >();
		for ( T value : possibleValues ) {
			values.put( value.name(), value );
		}
		this.values = Collections.unmodifiableMap( values );
	}

	@Override
	public String convertToString( T value ) {
		return value.name();
	}

	@Override
	public Optional< T > getFrom( String value ) {
		return Optional.ofNullable( values.get( value ) );
	}

	@Override
	public Collection< String > getValues() {
		return values.keySet();
	}

	@Override
	public T getDefault() {
		return defaultValue;
	}
	
	public static < T extends Enum< T > > SettingStateEnum< T > of( String id, boolean isPrivate, Class< T  > clazz, T defaultValue ) {
		return of( id, isPrivate, clazz, defaultValue, clazz.getEnumConstants() );
	}
	
	@SafeVarargs
	public static < T extends Enum< T > > SettingStateEnum< T > of( String id, boolean isPrivate, Class< T  > clazz, T defaultValue, T... values ) {
		return new SettingStateEnum< T >( id, isPrivate, clazz, defaultValue, values );
	}
}
