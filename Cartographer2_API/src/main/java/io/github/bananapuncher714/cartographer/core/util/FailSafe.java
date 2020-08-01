package io.github.bananapuncher714.cartographer.core.util;

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
	
	public static String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}
