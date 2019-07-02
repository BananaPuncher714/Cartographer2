package io.github.bananapuncher714.cartographer.core.map.text;

import java.awt.Font;

public enum FontStyle {
	BOLD( Font.BOLD ),
	ITALIC( Font.ITALIC ),
	PLAIN( Font.PLAIN ),
	HANGING_BASELINE( Font.HANGING_BASELINE ),
	CENTER_BASELINE( Font.CENTER_BASELINE ),
	ROMAN_BASELINE( Font.ROMAN_BASELINE );
	
	public final int style;
	
	FontStyle( int style ) {
		this.style = style;	
	}
}
