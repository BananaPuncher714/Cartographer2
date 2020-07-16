package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.Validate;

import com.google.common.io.ByteStreams;

import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

public class ModuleClassLoader extends URLClassLoader {
	static {
		ClassLoader.registerAsParallelCapable();
	}
	
	private final Map< String, Class< ? > > classes = new ConcurrentHashMap< String, Class< ? > >();
	private final Map< String, Class< ? > > internalClasses = new ConcurrentHashMap< String, Class< ? > >();
	private final ModuleDescription description;
	private final File file;
	private final URL url;
	private final JarFile jar;
	private final Manifest manifest;
	private final Module module;
	
	protected ModuleClassLoader( ModuleDescription description, ClassLoader parent ) throws IOException, ClassNotFoundException {
		super( new URL[] { description.getFile().toURI().toURL() }, parent );
		Validate.notNull( description );
		this.file = description.getFile();
		this.description = description;
		this.url = this.file.toURI().toURL();
		this.jar = new JarFile( this.file );
		this.manifest = this.jar.getManifest();
		
		Class< ? > jarClass = null;
		try {
			jarClass = Class.forName( description.getMain(), true, this );
			Class< ? extends Module > moduleClass = jarClass.asSubclass( Module.class );
			
			module = moduleClass.newInstance();
		} catch ( InstantiationException e ) {
			close();
			throw new IllegalArgumentException( "No public constructor " + jarClass );
		} catch ( IllegalAccessException e ) {
			close();
			throw new IllegalArgumentException( "Abnormal module type " + e );
		} catch ( ClassNotFoundException e ) {
			close();
			throw e;
		}
	}
	
	@Override
	protected Class< ? > findClass( String name ) throws ClassNotFoundException {
		return findClass( name, true );
	}
	
	private Class< ? > findClass( String name, boolean checkGlobal ) throws ClassNotFoundException {
		// First check if the class we're looking for is cached.
		Class< ? > result = ( Class< ? > ) this.classes.get( name );

		// If our class isn't already cached, then we'll want to load it some other way
		if ( result == null ) {
			// Check for global classes from plugins
			if ( checkGlobal ) {
				result = BukkitUtil.getClassByName( name );
			}

			// If we still don't have a class, then attempt to load it ourselves first
			if ( result == null ) {
				// Convert our class name to a path
				String path = name.replace( '.', '/' ).concat( ".class" );
				JarEntry entry = this.jar.getJarEntry( path );
				
				// See if it exists. If so...
				if ( entry != null ) {
					byte[] classBytes;
					String pkgName;
					definePackage : {
						// Try and load the class bytecode
						try ( InputStream is = this.jar.getInputStream( entry ) ) {
							classBytes = ByteStreams.toByteArray( ( InputStream ) is );
						} catch ( IOException ex ) {
							throw new ClassNotFoundException(name, ex);
						}
						
						// Whatever code asm stuf goes here
						// Probably parsing of legacy materials
						// TODO Do that sometime in the future, if applicable
						// classBytes = this.loader.server.getUnsafe().processClass( this.description, path, classBytes );
						
						int dot = name.lastIndexOf( '.' );
						// Check if it has a package
						if ( dot != -1 && this.getPackage(pkgName = name.substring( 0, dot ) ) == null ) {
							try {
								if ( this.manifest != null ) {
									definePackage( pkgName, this.manifest, this.url );
								} else {
									definePackage( pkgName, null, null, null, null, null, null, null );
								}
							} catch ( IllegalArgumentException ex ) {
								// Not sure what this part is for
								if ( this.getPackage( pkgName ) != null ) {
									break definePackage;
								}
								throw new IllegalStateException("Cannot find package " + pkgName);
							}
						}
					}
					
					CodeSigner[] signers = entry.getCodeSigners();
					CodeSource source = new CodeSource( url, signers );

					// Define our class
					result = defineClass( name, classBytes, 0, classBytes.length, source );
				}
				
				// If that didn't load or anything, then try the parent class loader. This is a child first class loader, after all.
				if ( result == null ) {
					result = super.findClass( name );
				} else {
					internalClasses.put( name, result );
				}

				if ( result != null ) {
					BukkitUtil.setClassToJavaPluginLoader( name, result );
				}
			} 

			// Cache our class
			classes.put( name, result );
		} 

		return result;
	}


	public void close() throws IOException {
		try {
			super.close();
		} finally {
			this.jar.close();
		}
	}
	
	public Module getModule() {
		return module;
	}
	
	public ModuleDescription getDescription() {
		return description;
	}
	
	protected Set< String > getClassNames() {
		return internalClasses.keySet();
	}
}
