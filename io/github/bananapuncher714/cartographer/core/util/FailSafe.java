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
	 * @param value
	 * The name of the enum.
	 * @return
	 * The enum with the name provided, or the first enum available.
	 */
	@SuppressWarnings("unchecked")
	public static < T extends Enum<?> > T getEnum( Class< T > clazz, String value ) {
		if ( !clazz.isEnum() ) return null;
		T[] constants = clazz.getEnumConstants();
		if ( value == null ) return constants[ 0 ];
		for ( Object object : constants ) {
			if ( object.toString().equals( value ) ) {
				return ( T ) object;
			}
		}
		return constants[ 0 ];
	}
}
