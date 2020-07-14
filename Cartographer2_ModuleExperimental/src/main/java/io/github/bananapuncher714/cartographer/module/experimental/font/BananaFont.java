package io.github.bananapuncher714.cartographer.module.experimental.font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.map.MapFont;

public class BananaFont extends MapFont {
	private String name;
	private String version;
	private String description;
	
	private BananaFont( String name, String description, String version ) {
		this.name = name;
		this.description = description;
		this.version = version;
		
		malleable = true;
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
	
	public static BananaFont from( String name, String description, String version ) {
		return new BananaFont( name, description, version );
	}
	
	public static BananaFont from( InputStream stream ) throws IOException {
		// Convert the input stream to a byte array
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[ 16384 ];
		while ( ( nRead = stream.read( data, 0, data.length ) ) != -1 ) buffer.write( data, 0, nRead );
		byte[] arr = buffer.toByteArray();
		
		// Get the binary reader
		BinaryReader reader = new BinaryReader( arr );
		
		String id = reader.getString( 2 );
		if ( !id.equals( "BF" ) ) throw new IllegalArgumentException( "Invalid header!" );
		
		int format = reader.getInt16();
		if ( format == 1 ) return parseFormat1( reader );

		throw new IllegalArgumentException( String.format( "Unsupported BananaTypeFont format %d", format ) );
	}
	
	private static BananaFont parseFormat1( BinaryReader reader ) {
		int dataOffset = reader.getInt16();
		String name = reader.getString( reader.getInt8() );
		String version = reader.getString( reader.getInt8() );
		String desc = reader.getString( reader.getInt8() );
		
		BananaFont font = new BananaFont( name, version, desc );
		
		int maxWidth = 0;
		
		reader.seek( dataOffset );
		while ( reader.pos() < reader.length() ) {
			int c = reader.getInt16();
			int width = reader.getInt8();
			int height = reader.getInt8();
			
			maxWidth = Math.max( maxWidth, width );
			
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
				
				font.setChar( ( char ) c, new CharacterSprite( width, height, values ) );
			}
		}
		font.setChar( ' ', new CharacterSprite( maxWidth >> 3, 0, new boolean[ 0 ] ) );
		
		return font;
	}
	
	private static class BinaryReader {
		private int pos;
		private byte[] data;
		
		public BinaryReader( byte[] buffer ) {
			this.pos = 0;
			this.data = buffer;
		}
		
		public int seek( int newPos ) {
			int oldPos = pos;
			pos = newPos;
			return oldPos;
		}
		
		public int pos() {
			return pos;
		}
		
		public int getInt8() {
			if ( pos >= data.length ) return 0;
			byte v = data[ pos++ ];
			if ( v < 0 ) {
				return 1 << 7 | ( v & 0b1111111 );
			}
			return v;
		}
		
		public int getInt16() {
			return getInt8() | getInt8() << 8;
		}
		
		public String getString( int len ) {
			StringBuilder b = new StringBuilder();
			for ( int i = 0; i < len; i++ ) {
				b.append( ( char ) getInt8() );
			}
			return b.toString();
		}
		
		public int length() {
			return data.length;
		}
	}
}
