package io.github.bananapuncher714.cartographer.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class ZipWrapper {
	protected final File zipFile;
	protected FileSystem zipfs;
	
	public ZipWrapper( File resource ) throws IOException {
		zipFile = resource;
		resource.getParentFile().mkdirs();
		if ( !resource.exists() ) {
			ZipOutputStream out = new ZipOutputStream( new FileOutputStream( resource ) );
			out.close();
		}
	}
	
	public File getFile() {
		return zipFile;
	}
	
	public void addElement( String path, byte[] write, OpenOption... options ) throws IOException {
		FileSystem zipfs = getZipfs();
		Path internal = zipfs.getPath( path );
		if ( internal.getParent() != null ) {
			Files.createDirectories( internal.getParent() );
		}
		Files.write( internal, write, options );
	}

	public void addElement( String path, File file, CopyOption... options ) throws IOException {
		FileSystem zipfs = getZipfs();
		Path external = Paths.get( file.getAbsolutePath() );
		Path internal = zipfs.getPath( path );
		if ( internal.getParent() != null ) {
			Files.createDirectories( internal.getParent() );
		}
		Files.copy( external, internal, options );
	}
	
	public InputStream readElement( String path, OpenOption... options  ) throws IOException {
		FileSystem zipfs = getZipfs();
		Path internal = zipfs.getPath( path );
		if ( internal != null ) {
			if ( Files.exists( internal ) ) {
				return Files.newInputStream( internal, options );
			}
		}
		return null;
	}

	public void removeElement( String path, File callback, CopyOption... options ) throws IOException {
		FileSystem zipfs = getZipfs();
		Path internal = zipfs.getPath( path );
		if ( internal != null ) {
			if ( !Files.exists( internal ) ) {
				return;
			}
			if ( callback != null ) {
				callback.getParentFile().mkdirs();
				Files.move( internal, Paths.get( callback.getAbsolutePath() ), options );
			} else {
				Files.delete( internal );
			}
		}
	}
	
	public List< String > listElements( String path ) throws IOException {
		FileSystem zipfs = getZipfs();
		Path internal = zipfs.getPath( path );
		if ( internal != null ) {
			if ( !Files.exists( internal ) ) {
				return null;
			}
			List< String > files = new ArrayList< String >();
			for ( Path file: Files.newDirectoryStream( internal ) ) {
				files.add( file.getFileName().toString() );
			}
			return files;
		}
		return null;
	}
	
	private FileSystem getZipfs() throws IOException {
		if ( zipfs == null ) {
			zipfs = FileSystems.newFileSystem( Paths.get( zipFile.getAbsolutePath() ), ( ClassLoader ) null );
		}
		return zipfs;
	}
	
	public boolean close() throws IOException {
		if ( zipfs != null && zipfs.isOpen() ) {
			zipfs.close();
			zipfs = null;
			return true;
		}
		zipfs = null;
		return false;
	}
}

