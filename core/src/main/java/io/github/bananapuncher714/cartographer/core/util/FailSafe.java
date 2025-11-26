package io.github.bananapuncher714.cartographer.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.map.MapCursor.Type;

/**
 * Utility methods to prevent null values.
 * 
 * @author BananaPuncher714
 */
public final class FailSafe {
	/**
	 * Get a valid enum value no matter what.
	 * 
	 * @param clazz
	 * The enum class.
	 * @param values
	 * The name of the enums.
	 * @return
	 * The enum with the name provided, or the first enum available.
	 */
	@SuppressWarnings("unchecked")
	public static < T extends Enum<?> > T getEnum( Class< T > clazz, String... values ) {
		if ( !clazz.isEnum() ) return null;
		T[] constants = clazz.getEnumConstants();
		if ( values == null || values.length == 0 ) return constants[ 0 ];
		for ( Object object : constants ) {
			if ( object.toString().equals( values[ 0 ] ) ) {
				return ( T ) object;
			}
		}
		return getEnum( clazz, pop( values ) );
	}
	
   public static Type getType( String... types ) {
        try {
            Method values = Type.class.getMethod( "values" );
            Method name = Type.class.getMethod( "name" );
            Type[] constants = ( Type[] ) values.invoke( Type.class );
            if ( types == null || types.length == 0 ) return constants[ 0 ];
            if ( types[ 0 ].equals( "RED_MARKER" ) ) types[ 0 ] = "TARGET_POINT";
            for ( Type t : constants ) {
                if ( name.invoke( t ).equals( types[ 0 ] ) ) {
                    return t;
                }
            }
            return getType( FailSafe.pop( types ) );
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
        }
        return null;
    }
	
	public static String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}
