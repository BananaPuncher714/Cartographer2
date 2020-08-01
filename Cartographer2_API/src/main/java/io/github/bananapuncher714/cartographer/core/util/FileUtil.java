package io.github.bananapuncher714.cartographer.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Simple file management including YAML updaters
 * 
 * @author BananaPuncher714
 */
public final class FileUtil {
	public static void saveAndUpdate( InputStream stream, File output, boolean trim ) {
		if ( !output.exists() ) {
			saveToFile( stream, output, false );
		}
		updateConfigFromFile( output, stream, trim );
	}

	public static void saveToFile( InputStream stream, File output, boolean force ) {
		if ( force && output.exists() ) {
			output.delete();
		}
		if ( !output.exists() ) {
			output.getParentFile().mkdirs();
			try ( OutputStream outStream = new FileOutputStream( output ) ) {
				byte[] buffer = new byte[ stream.available() ];

				int len;
				while ( ( len = stream.read( buffer ) ) > 0)  {
					outStream.write( buffer, 0, len );
				}
				stream.close();
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	public static void updateConfigFromFile( File toUpdate, InputStream toCopy ) {
		updateConfigFromFile( toUpdate, toCopy, false );
	}

	public static void updateConfigFromFile( File toUpdate, InputStream toCopy, boolean trim ) {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new InputStreamReader( toCopy ) );
		FileConfiguration old = YamlConfiguration.loadConfiguration( toUpdate );

		for ( String key : config.getKeys( true ) ) {
			if ( !old.contains( key ) ) {
				old.set( key, config.get( key ) );
			}
		}

		if ( trim ) {
			for ( String key : old.getKeys( true ) ) {
				if ( !config.contains( key ) ) {
					old.set( key, null );
				}
			}
		}

		try {
			old.save( toUpdate );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	public static boolean move( File original, File dest, boolean force ) {
		if ( dest.exists() ) {
			if ( !force ) {
				return false;
			} else {
				recursiveDelete( dest );
			}
		}
		dest.getParentFile().mkdirs();
		original.renameTo( dest );
		recursiveDelete( original );
		return true;
	}

	public static void recursiveDelete( File file ) {
		if ( !file.exists() ) {
			return;
		}
		if ( file.isDirectory() ) {
			for ( File lower : file.listFiles() ) {
				recursiveDelete( lower );
			}
		}
		file.delete();
	}

	public static < T extends Serializable > T readObject( Class< T > clazz, File file ) throws IOException, ClassNotFoundException {
		if ( !file.exists() ) {
			return null;
		}
		T head = null;
		FileInputStream fis = new FileInputStream( file );
		ObjectInputStream ois = new ObjectInputStream( fis );

		head = ( T ) ois.readObject();

		ois.close();
		fis.close();
		return head;
	}

	public static void writeObject( Serializable object, File file ) {
		file.getParentFile().mkdirs();
		try {
			FileOutputStream fos = new FileOutputStream( file );
			ObjectOutputStream oos = new ObjectOutputStream( fos );

			oos.writeObject( object );

			oos.close();
			fos.close();
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	public static File getImageFile( File dir, String prefix ) {
		File image = new File( dir + "/" + prefix + ".png" );
		for ( File file : dir.listFiles() ) {
			String fileName = file.getName();
			if ( fileName.equalsIgnoreCase( prefix + ".gif" ) ) {
				return file;
			} else if ( fileName.equalsIgnoreCase( prefix + ".png" ) || fileName.equalsIgnoreCase( prefix + ".jpg" ) || fileName.equalsIgnoreCase( prefix + ".jpeg" ) || fileName.equalsIgnoreCase( prefix + ".bmp" ) ) {
				image = file;
			}
		}
		return image;
	}
}
