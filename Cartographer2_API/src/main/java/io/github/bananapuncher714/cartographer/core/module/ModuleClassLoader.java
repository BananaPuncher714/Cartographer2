package io.github.bananapuncher714.cartographer.core.module;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Set;

public class ModuleClassLoader extends URLClassLoader {
	protected ModuleClassLoader( ModuleDescription description, ClassLoader parent ) throws IOException, ClassNotFoundException {
		super( null );
	}
	
	@Override
	protected Class< ? > findClass( String name ) throws ClassNotFoundException {
		return null;
	}
	

	public void close() throws IOException {
	}
	
	public Module getModule() {
		return null;
	}
	
	public ModuleDescription getDescription() {
		return null;
	}
	
	protected Set< String > getClassNames() {
		return null;
	}
}
