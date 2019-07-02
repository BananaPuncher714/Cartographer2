package io.github.bananapuncher714.cartographer.core.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.Bukkit;

import io.github.bananapuncher714.cartographer.core.api.PacketHandler;

/**
 * Internal use only
 * 
 * @author BananaPuncher714
 */
public final class ReflectionUtil {
	public static final String VERSION;
	
	static {
		VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

	private ReflectionUtil() {
	}
	
	public static PacketHandler getNewPacketHandlerInstance() {
		try {
			Class< ? > clazz = Class.forName( "io.github.bananapuncher714.cartographer.core.implementation." + VERSION + ".NMSHandler" );
			return ( PacketHandler ) clazz.newInstance();
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	// TODO Add modular loading
	public static void loadJar( File file ) {
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
	}
}
