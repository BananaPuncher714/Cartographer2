package io.github.bananapuncher714.cartographer.core.file;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;

/**
 * Save and load {@link BigChunk} asynchronously.
 * 
 * @author BananaPuncher714
 */
public class BigChunkQueue {
	protected final ExecutorService savingService = null;
	protected final ExecutorService loadingService = null;
	
	protected Map< BigChunkLocation, Future< BigChunk > > loading;
	protected Map< BigChunkLocation, Future< Boolean > > saving;
	
	protected MapDataCache cache;
	protected File saveLocation;
	
	/**
	 * Construct a BigChunkQueue from the arguments provided. 
	 * 
	 * @param saveFile
	 * The directory to save {@link BigChunk} in. Cannot be null.
	 * @param cache
	 * The {@link MapDataCache} containing the data. Cannot be null.
	 */
	public BigChunkQueue( File saveFile, MapDataCache cache ) {
	}
	
	/**
	 * Save the data as soon as possible, if not being already saved or loaded.
	 * 
	 * @param coord
	 * The {@link BigChunkLocation} of the data, cannot be null.
	 * @param data
	 * The {@link BigChunk} to save, cannot be null.
	 * @return
	 * If successfully queued for saving.
	 */
	public boolean save( BigChunkLocation coord, BigChunk data ) {
		return true;
	}
	
	/**
	 * Shutdown loading and saving services, and finish saving what needs to be saved on the main thread.
	 * 
	 * @return
	 * If shutting down was successful.
	 */
	public boolean saveBlocking() {
		return true;
	}
	
	/**
	 * Load the {@link BigChunkLocation} as soon as possible.
	 * 
	 * @param coord
	 * The {@link BigChunkLocation} to load, cannot be null.
	 */
	public void load( BigChunkLocation coord ) {
	}
	
	/**
	 * Should be called each tick or so often by whatever is responsible for this
	 * to use the data that has been loaded, and request processing for what has not.
	 */
	public void update() {
	}
	
	public boolean isSaving( ChunkLocation location ) {
		return true;
	}
	
	public boolean isLoading( ChunkLocation location ) {
		return true;
	}
	
	/**
	 * Get the file for the {@link BigChunkLocation}.
	 * 
	 * @param coord
	 * The coordinate of the {@link BigChunkLocation}, cannot be null.
	 * @return
	 * Normally stored in 'base/world/x/z/' form.
	 */
	protected File getFileFor( BigChunkLocation coord ) {
		return null;
	}
	
	/**
	 * Responsible for saving chunks.
	 * 
	 * @author BananaPuncher714
	 */
	protected class TaskChunkSave implements Callable< Boolean > {
		protected final File saveFile;
		protected final BigChunk chunk;
		
		/**
		 * Save a BigChunk to the file.
		 * 
		 * @param saveFile
		 * Cannot be null.
		 * @param chunk
		 * Cannot be null.
		 */
		TaskChunkSave( File saveFile, BigChunk chunk ) {
			this.saveFile = saveFile;
			this.chunk = chunk;
		}
		
		@Override
		public Boolean call() throws Exception {
			return true;
		}
	}
	
	/**
	 * Responsible for loading chunks.
	 * 
	 * @author BananaPuncher714
	 */
	protected class TaskChunkLoad implements Callable< BigChunk > {
		protected final File file;
		
		/**
		 * Load a BigChunk from the file provided.
		 * 
		 * @param file
		 * Cannot be null.
		 */
		TaskChunkLoad( File file ) {
			this.file = file;
		}
		
		@Override
		public BigChunk call() throws Exception {
			return null;
		}
	}
}
