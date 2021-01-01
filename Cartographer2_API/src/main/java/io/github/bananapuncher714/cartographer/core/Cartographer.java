package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.command.CommandCartographer;
import io.github.bananapuncher714.cartographer.core.dependency.DependencyManager;
import io.github.bananapuncher714.cartographer.core.locale.LocaleManager;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class Cartographer extends JavaPlugin {
	protected void onServerLoad() {
	}
	
	/**
	 * Purely for configs, palettes and images
	 * Does not load modules
	 */
	public void reload() {
	}
	
	public File getMapDirFor( String id ) {
		return null;
	}
	
	public File getAndConstructMapDir( String id ) {
		return null;
	}
	
	protected void saveMapFiles( File dir ) {
	}
	
	public PacketHandler getHandler() {
		return null;
	}
	
	public CommandCartographer getCommand() {
		return null;
	}
	
	public MinimapManager getMapManager() {
		return null;
	}
	
	public PaletteManager getPaletteManager() {
		return null;
	}
	
	public ModuleManager getModuleManager() {
		return null;
	}
	
	public DependencyManager getDependencyManager() {
		return null;
	}
	
	public PlayerManager getPlayerManager() {
		return null;
	}
	
	public LocaleManager getLocaleManager() {
		return null;
	}
	
	public CartographerSettings getSettings() {
		return null;
	}
	
	public void setSettings( CartographerSettings settings ) {
	}
	
	public Map< Integer, CartographerRenderer > getRenderers() {
		return null;
	}

	public boolean isServerOverloaded() {
		return false;
	}
	
	
	public int getTickLimit() {
		return 0;
	}
	
	public void setTickLimit( int limit ) {
	}
	
	
	public static Cartographer getInstance() {
		return null;
	}
	
	public static GeneralUtil getUtil() {
		return null;
	}
	
	public static File getMapSaveDir() {
		return null;
	}
	
	public static File getModuleDir() {
		return null;
	}
	
	public static File getPaletteDir() {
		return null;
	}
	
	public static File getCacheDir() {
		return null;
	}
}
