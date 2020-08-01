package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.primitives.Primitives;

public class CommandParameters {
	protected List< Object > parameters = new ArrayList< Object >();

	public CommandParameters() {
	}
	
	public CommandParameters( CommandParameters copy ) {
		parameters.addAll( copy.parameters );
	}
	
	public void add( Object object ) {
		parameters.add( object );
	}
	
	public int size() {
		return parameters.size();
	}
	
	public < T > T get( Class < T > clazz, int index ) {
		clazz = Primitives.wrap( clazz );
		if ( index < 0 || index >= parameters.size() ) {
			return null;
		}
		Object obj = parameters.get( index );
		if ( obj != null && clazz.isInstance( obj ) ) {
			return clazz.cast( obj );
		}
		return null;
	}
	
	public < T > T getLast( Class< T > clazz ) {
		clazz = Primitives.wrap( clazz );
		for ( int i = parameters.size() - 1; i >= 0; i-- ) {
			Object obj = parameters.get( i );
			if ( obj != null && clazz.isInstance( obj ) ) {
				return clazz.cast( obj );
			}
		}
		return null;
	}
	
	public < T > T getFirst( Class< T > clazz ) {
		clazz = Primitives.wrap( clazz );
		for ( int i = 0; i < parameters.size(); i++ ) {
			Object obj = parameters.get( i );
			if ( obj != null && clazz.isInstance( obj ) ) {
				return clazz.cast( obj );
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "CommandParameters[" + parameters.stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) + "]";
	}
}
