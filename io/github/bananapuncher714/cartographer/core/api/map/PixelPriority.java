package io.github.bananapuncher714.cartographer.core.api.map;

public enum PixelPriority {
	LOWEST( 0 ), LOW( 1 ), NORMAL( 2 ), HIGH( 3 ), HIGHEST( 4 );
	
	private final int index;
	
	PixelPriority( int index ) {
		this.index = index;
	}
	
	public boolean isLowerThan( PixelPriority priority ) {
		return priority.index > index;
	}
	
	public boolean isHigherThan( PixelPriority priority ) {
		return priority.index < index;
	}
}
