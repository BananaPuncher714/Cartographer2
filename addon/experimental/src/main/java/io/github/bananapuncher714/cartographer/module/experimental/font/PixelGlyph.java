package io.github.bananapuncher714.cartographer.module.experimental.font;

public class PixelGlyph implements Comparable< PixelGlyph > {
	private int c;
	private int width;
	private int height;
	private boolean[] data;
	
	public PixelGlyph( int c ) {
		this.c = c;
	}
	
	public char getChar() {
		return ( char ) c;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public boolean[] getData() {
		return data;
	}

	public void setData( boolean[] data ) {
		this.data = data;
	}

	@Override
	public int compareTo( PixelGlyph other ) {
		if ( c < other.c ) {
			return -1;
		} else if ( c > other.c ) {
			return 1;
		}
		return 0;
	}
}
