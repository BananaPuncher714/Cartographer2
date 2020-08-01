package io.github.bananapuncher714.cartographer.core.api;

/**
 * Boolean enum with UNSET option.
 * 
 * @author BananaPuncher714
 */
public enum BooleanOption {
	TRUE, FALSE, UNSET;
	
	/**
	 * Check for true.
	 * 
	 * @return
	 * If this enum is equal to TRUE.
	 */
	public boolean isTrue() {
		return this == TRUE;
	}
	
	/**
	 * Check for unset.
	 * 
	 * @return
	 * If this enum is equal to UNSET.
	 */
	public boolean isUnset() {
		return this == UNSET;
	}
}
