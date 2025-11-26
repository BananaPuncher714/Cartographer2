package io.github.bananapuncher714.cartographer.module.experimental.font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.bananapuncher714.cartographer.module.experimental.util.BinaryReader;

public class BananaFontParser {
	public static BananaTypeFont createFont( InputStream stream ) throws IOException {
		byte[] arr = toByteArray( stream );
		BinaryReader reader = new BinaryReader( arr );
		
		String id = reader.getString( 2 );
		
		if ( !id.equals( "BF" ) ) {
			throw new IllegalArgumentException( "Invalid header!" );
		}
		
		int format = reader.getInt16Little();
		
		stream.close();
		
		if ( format == 1 ) {
			return parseFormat1( reader );
		}
		
		throw new IllegalArgumentException( String.format( "Unsupported BananaTypeFont format %d", format ) );
	}
	
	private static BananaTypeFont parseFormat1( BinaryReader reader ) {
		int dataOffset = reader.getInt16Little();
		String name = reader.getString( reader.getInt8() );
		String version = reader.getString( reader.getInt8() );
		String desc = reader.getString( reader.getInt8() );
		
		BananaTypeFont font = new BananaTypeFont( name, version, desc );
		
		reader.seek( dataOffset );
		while ( reader.pos() < reader.length() ) {
			int c = reader.getInt16Little();
			int width = reader.getInt8();
			int height = reader.getInt8();
			PixelGlyph glyph = new PixelGlyph( c );
			glyph.setWidth( width );
			glyph.setHeight( height );
			font.put( glyph.getChar(), glyph );
			
			int size = width * height;
			if ( size > 0 ) {
				boolean[] values = new boolean[ size ];
				int index = 0;
				while ( index < size ) {
					int v = reader.getInt8();
					for ( int i = 7; i >= 0 && index < size; i-- ) {
						values[ index++ ] = ( ( v >> i ) & 1 ) == 1;
					}
				}
				glyph.setData( values );
			}
		}
		
		return font;
	}
	
	private static byte[] toByteArray( InputStream stream ) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[ 16384 ];

		while ( ( nRead = stream.read( data, 0, data.length ) ) != -1 ) {
		  buffer.write( data, 0, nRead );
		}

		return buffer.toByteArray();
	}
}
