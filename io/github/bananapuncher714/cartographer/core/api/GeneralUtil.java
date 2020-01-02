package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * Different Bukkit methods that change across various Minecraft versions.
 * 
 * @author BananaPuncher714
 */
public interface GeneralUtil {
	/**
	 * Get the a MapView from a map item.
	 * 
	 * @param item
	 * A map item, may be null.
	 * @return
	 * Null if the item is null, or not a map.
	 */
	MapView getMapViewFrom( ItemStack item );
	
	/**
	 * Get a MapView for the id provided.
	 * 
	 * @param id
	 * The id of the map, may not work for values above 32767 depending on the version of Minecraft.
	 * @return
	 * A Bukkit MapView.
	 */
	MapView getMap( int id );
	
	/**
	 * Get the id of the MapView as an integer.
	 * 
	 * @param view
	 * Cannot be null.
	 * @return
	 * An integer representing the id.
	 */
	int getId( MapView view );
	
	/**
	 * Get a filled map item with the mapview of the given id.
	 * 
	 * @param id
	 * An integer, may have to be below 32767 depending on the version of Minecraft.
	 * @return
	 * An ItemStack of the filled map material.
	 */
	ItemStack getMapItem( int id );
	
	/**
	 * Get the material for a filled map.
	 * 
	 * @return
	 * Actual enum may differ depending on the version of Minecraft.
	 */
	Material getMapMaterial();
	
	/**
	 * Check if the event is called with the player's main hand.
	 * 
	 * @param event
	 * Cannot be null.
	 * @return
	 * If the event's hand is equal to the player's main hand, or true by default in earlier versions of Minecraft.
	 */
	boolean isValidHand( PlayerInteractEvent event );
	
	/**
	 * Check if a material is water.
	 * 
	 * @param material
	 * Includes flowing water as water.
	 * @return
	 * Whether the material provided is some form of water.
	 */
	boolean isWater( Material material );
	
	/**
	 * Get the material of the block in the chunk snapshot at the specified coordinates.
	 * 
	 * @param snapshot
	 * Cannot be null.
	 * @param x
	 * Local x value from 0 to 15.
	 * @param y
	 * Local y value from 0 to 255.
	 * @param z
	 * Local z value from 0 to 15.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z );
	
	/**
	 * Get the material type of the item.
	 * 
	 * @param item
	 * Cannot be null.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getItemType( ItemStack item );
	
	/**
	 * Get the material type of the block.
	 * 
	 * @param block
	 * Cannot be null.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getBlockType( Block block );
	
	/**
	 * Check if this event is significant enough to require updating.
	 * 
	 * @param event
	 * Cannot be null.
	 * @return
	 * If the block being updated is the source block.
	 */
	boolean updateEvent( BlockPhysicsEvent event );
}
