package io.github.bananapuncher714.cartographer.core.util;

import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.nbteditor.NBTEditor;

/**
 * Internal use only
 * 
 * @author BananaPuncher714
 */
public final class ReflectionUtil {
	private ReflectionUtil() {
	}
	
	public static PacketHandler getNewPacketHandlerInstance() {
		try {
			Class< ? > clazz = Class.forName( "io.github.bananapuncher714.cartographer.core.implementation." + NBTEditor.getMinecraftVersion().name() + ".NMSHandler" );
			return ( PacketHandler ) clazz.newInstance();
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}
}
