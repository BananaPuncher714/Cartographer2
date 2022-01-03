package io.github.bananapuncher714.cartographer.core.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.DataCache;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

/**
 * Save and load {@link BigChunk} asynchronously.
 * 
 * @author BananaPuncher714
 */
public class BigChunkQueue {
	protected final ExecutorService savingService = Executors.newFixedThreadPool( 2 );
	protected final ExecutorService loadingService = Executors.newFixedThreadPool( 2 );
	
	protected Map< BigChunkLocation, Future< BigChunk > > loading = new ConcurrentHashMap< BigChunkLocation, Future< BigChunk > >();
	protected Map< BigChunkLocation, Future< Boolean > > saving = new ConcurrentHashMap< BigChunkLocation, Future< Boolean > >();
	
	protected DataCache cache;
	protected Path saveLocation;
	
	/**
	 * Construct a BigChunkQueue from the arguments provided. 
	 * 
	 * @param saveFile
	 * The directory to save {@link BigChunk} in. Cannot be null.
	 * @param cache
	 * The {@link MapDataCache} containing the data. Cannot be null.
	 */
	public BigChunkQueue( Path saveFile, DataCache cache ) {
		Validate.notNull( saveFile );
		Validate.notNull( cache );
		saveLocation = saveFile;
		this.cache = cache;
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
		Validate.notNull( coord );
		if ( saving.containsKey( coord ) || loading.containsKey( coord ) ) {
			return false;
		}
		Validate.notNull( data );
		saving.put( coord, savingService.submit( new TaskChunkSave( getFileFor( coord ), data ) ) );
		return true;
	}
	
	/**
	 * Shutdown loading and saving services, and finish saving what needs to be saved on the main thread.
	 * 
	 * @return
	 * If shutting down was successful.
	 */
	public boolean saveBlocking() {
		loadingService.shutdown();
		savingService.shutdown();
		try {
			savingService.awaitTermination( 3, TimeUnit.MINUTES );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
			return false;
		}
		List< Runnable > leftovers = savingService.shutdownNow();
		for ( Runnable runnable : leftovers ) {
			runnable.run();
		}
		
		return true;
	}
	
	/**
	 * Load the {@link BigChunkLocation} as soon as possible.
	 * 
	 * @param coord
	 * The {@link BigChunkLocation} to load, cannot be null.
	 */
	public boolean load( BigChunkLocation coord ) {
		Validate.notNull( coord );
		// If the chunk is not being loaded or saved
		boolean inUse = saving.containsKey( coord ) || loading.containsKey( coord );
		if ( !inUse ) {
			loading.put( coord, loadingService.submit( new TaskChunkLoad( getFileFor( coord ) ) ) );
			return true;
		}
		return false;
	}
	
	/**
	 * Should be called each tick or so often by whatever is responsible for this
	 * to use the data that has been loaded, and request processing for what has not.
	 */
	public void update() {
		for ( Iterator< Entry< BigChunkLocation, Future< Boolean > > > iterator = saving.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< BigChunkLocation, Future< Boolean > > entry = iterator.next();
			
			if ( entry.getValue().isDone() ) {
				iterator.remove();
			}
		}
		
		for ( Iterator< Entry< BigChunkLocation, Future< BigChunk > > > iterator = loading.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< BigChunkLocation, Future< BigChunk > > entry = iterator.next();
			
			// Check if it is done loading from file or something
			if ( entry.getValue().isDone() ) {
				BigChunkLocation coord = entry.getKey();
				BigChunk value = null;
				try {
					value = entry.getValue().get();
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
				
				int relX = coord.getX() << 4;
				int relZ = coord.getZ() << 4;
				// Check if the file was loaded successfully
				if ( value != null ) {
					// There *was* something on file after all
					cache.updateDataAt( coord, value, false );
				} else {
					for ( int x = 0; x < 16; x++ ) {
						for ( int z = 0; z < 16; z++ ) {
							ChunkLocation location = new ChunkLocation( coord.getWorld(), relX + x, relZ + z );
							// Request the cache to load our location
							cache.requestLoadFor( location, false );
						}
					}
				}
				iterator.remove();
			}
		}
	}
	
	public boolean isSaving( ChunkLocation location ) {
		return saving.containsKey( new BigChunkLocation( location ) );
	}
	
	public boolean isLoading( ChunkLocation location ) {
		return loading.containsKey( new BigChunkLocation( location ) );
	}
	
	/**
	 * Get the file for the {@link BigChunkLocation}.
	 * 
	 * @param coord
	 * The coordinate of the {@link BigChunkLocation}, cannot be null.
	 * @return
	 * Normally stored in 'base/world/x/z/' form.
	 */
	protected Path getFileFor( BigChunkLocation coord ) {
		Validate.notNull( coord );
		return Paths.get( saveLocation + "/" + coord.getWorld().getName() + "/" + coord.getX() + "/" + coord.getZ() );
	}
	
	/**
	 * Responsible for saving chunks.
	 * 
	 * @author BananaPuncher714
	 */
	protected class TaskChunkSave implements Callable< Boolean > {
		protected final Path saveFile;
		protected final BigChunk chunk;
		
		/**
		 * Save a BigChunk to the file.
		 * 
		 * @param saveFile
		 * Cannot be null.
		 * @param chunk
		 * Cannot be null.
		 */
		TaskChunkSave( Path saveFile, BigChunk chunk ) {
			Validate.notNull( chunk );
			Validate.notNull( saveFile );
			this.saveFile = saveFile;
			this.chunk = chunk;
		}
		
		@Override
		public Boolean call() throws Exception {
			// First check if the file exists previously
			if ( Files.isRegularFile( saveFile ) ) {
				// If so, merge the chunks
				BigChunk onDisk = FileUtil.readObject( BigChunk.class, saveFile.toFile() );
				
				for ( int i = 0; i < chunk.getData().length; i++ ) {
					if ( chunk.getData()[ i ] == null ) {
						// Only save the data if the chunk we need to save doesn't contain the chunk data
						// Either it's something, or null
						chunk.getData()[ i ] = onDisk.getData()[ i ];
					}
				}
			}
			FileUtil.writeObject( chunk, saveFile.toFile() );
			return true;
		}
	}
	
	/**
	 * Responsible for loading chunks.
	 * 
	 * @author BananaPuncher714
	 */
	protected class TaskChunkLoad implements Callable< BigChunk > {
		protected final Path file;
		
		/**
		 * Load a BigChunk from the file provided.
		 * 
		 * @param file
		 * Cannot be null.
		 */
		TaskChunkLoad( Path file ) {
			Validate.notNull( file );
			this.file = file;
		}
		
		@Override
		public BigChunk call() throws Exception {
			if ( Files.isRegularFile( file ) ) {
				try {
					return FileUtil.readObject( BigChunk.class, file.toFile() );
				} catch ( Exception exception ) {
					// Delete the file if there was a problem reading it.
					// Should probably catch the exception and log it, but will do that later.
					// TODO Fix this?
//					exception.printStackTrace();
					Files.delete( file );
					return null;
				}
			}
			return null;
		}
	}
}
