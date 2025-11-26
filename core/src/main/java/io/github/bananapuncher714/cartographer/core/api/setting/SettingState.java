package io.github.bananapuncher714.cartographer.core.api.setting;

import java.util.Collection;
import java.util.Optional;

public abstract class SettingState< T extends Comparable< T > > {
	private final String id;
	private final boolean isPrivate;
	private final Class< T > clazz;
	
	public SettingState( String id, boolean isPrivate, Class< T > clazz ) {
		if ( !id.matches( "\\S+" ) ) {
			throw new IllegalArgumentException( "'" + id + "' cannot contain any spaces!" );
		}
		
		this.id = id;
		this.isPrivate = isPrivate;
		this.clazz = clazz;
	}
	
	public final String getId() {
		return id;
	}
	
	public final boolean isPrivate() {
		return isPrivate;
	}
	
	public final Class< T > getType() {
		return clazz;
	}
	
	@Override
	public final boolean equals( Object obj ) {
		if ( this == obj ) {
			return true;
		}
		
		if ( obj instanceof SettingState ) {
			SettingState< ? > state = ( SettingState< ? > ) obj;
			
			return state.clazz == clazz && state.id.equals( id );
		}
		return false;
	}

	/**
	 * Convert the provided object to string.
	 * 
	 * @param value
	 * The value that needs converting.
	 * @return
	 * A string which may be used in serialization.
	 */
	public abstract String convertToString( T value );
	
	/**
	 * Get an optional from the provided string.
	 * 
	 * @param value
	 * A string representing a value of the object.
	 * @return
	 * An optional that may contain a valid object.
	 */
    public abstract Optional< T > getFrom( String value );
    
    /**
     * Get all the valid values that this state could take on.
     * 
     * @return
     * A collection of all valid states.
     */
    public abstract Collection< String > getValues();
    
    /**
     * Get the default object that would be provided normally
     * 
     * @return
     * The default object
     */
    public abstract T getDefault();
}
