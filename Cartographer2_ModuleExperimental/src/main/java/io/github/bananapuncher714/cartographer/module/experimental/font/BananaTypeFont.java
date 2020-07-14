package io.github.bananapuncher714.cartographer.module.experimental.font;

import java.util.HashMap;
import java.util.Map;

public class BananaTypeFont {
	private String name;
	private String version;
	private String description;
	private Map< Character, PixelGlyph > glyphs = new HashMap< Character, PixelGlyph >();
	private int maxHeight = 0;
	private int maxWidth = 0;
	
	public BananaTypeFont( String name, String version, String description ) {
		this.name = name;
		this.version = version;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}
	
	public int getMaxHeight() {
		return maxHeight;
	}

	public int getMaxWidth() {
		return maxWidth;
	}
	
	public PixelGlyph put( char c, PixelGlyph glyph ) {
		maxHeight = Math.max( glyph.getHeight(), maxHeight );
		maxWidth = Math.max( glyph.getWidth(), maxWidth );
		return glyphs.put( c, glyph );
	}
	
	public PixelGlyph get( char c ) {
		return glyphs.get( c );
	}
}
