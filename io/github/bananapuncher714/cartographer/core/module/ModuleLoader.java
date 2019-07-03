package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import io.github.bananapuncher714.cartographer.core.api.Module;
import io.github.bananapuncher714.cartographer.core.util.ReflectionUtil;

public class ModuleLoader {
	public static Module load( File file ) {
		try {
			URLClassLoader child = new URLClassLoader(
			        new URL[] { file.toURI().toURL() },
			        ReflectionUtil.class.getClassLoader()
			);
			Class classToLoad = Class.forName( "com.MyClass", true, child );
			Method method = classToLoad.getDeclaredMethod( "myMethod" );
			Object instance = classToLoad.newInstance();
			Object result = method.invoke(instance);
		} catch ( MalformedURLException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e ) {
			e.printStackTrace();
		}
		return null;
	}
}
