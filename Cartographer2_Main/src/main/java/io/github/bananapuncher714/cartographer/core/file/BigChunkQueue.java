package io.github.bananapuncher714.cartographer.core.file;

import java.io.File;
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
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
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
	public void load( BigChunkLocation coord ) {
		Validate.notNull( coord );
		// If the chunk is not being loaded or saved
		if ( saving.containsKey( coord ) || loading.containsKey( coord ) ) {
			return;
		}
		loading.put( coord, loadingService.submit( new TaskChunkLoad( getFileFor( coord ) ) ) );
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
				if ( value != null ) {
					// There *was* something on file after all
					ChunkData[] data = value.getData();
					
					for ( int index = 0; index < data.length; index++ ) {
						ChunkData chunkData = data[ index ];
						ChunkLocation location = new ChunkLocation( coord.getWorld(), relX + index % 16, relZ + index / 16 );
						if ( chunkData == null ) {
							// If it doesn't have the chunk data we're looking for, then don't bother
							cache.addToProcessQueue( location );
						} else if ( cache.absent( location ) ){
							// If the cache contains a more up to date version
							// then might as well skip this
							// Otherwise, add it in
							cache.loadData( location, chunkData );
						}
					}
				} else {
					// Nothing was loaded from file
					for ( int x = 0; x < 16; x++ ) {
						for ( int z = 0; z < 16; z++ ) {
							ChunkLocation location = new ChunkLocation( coord.getWorld(), relX + x, relZ + z );
							if ( cache.absent( location ) ) {
								// Only add it to the process queue if it's not already stored in the cache somewhere
								cache.addToProcessQueue( location );
							}
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
	protected File getFileFor( BigChunkLocation coord ) {
		Validate.notNull( coord );
		return new File( saveLocation + "/" + coord.getWorld().getName() + "/" + coord.getX() + "/" + coord.getZ() );
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
			Validate.notNull( chunk );
			Validate.notNull( saveFile );
			this.saveFile = saveFile;
			this.chunk = chunk;
		}
		
		@Override
		public Boolean call() throws Exception {
			FileUtil.writeObject( chunk, saveFile );
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
			Validate.notNull( file );
			this.file = file;
		}
		
		@Override
		public BigChunk call() throws Exception {
			if ( file.exists() ) {
				try {
					return FileUtil.readObject( BigChunk.class, file );
				} catch ( Exception exception ) {
					// Delete the file if there was a problem reading it.
					// Should probably catch the exception and log it, but will do that later.
					// TODO Fix this
					file.delete();
					return null;
				}
			}
			return null;
		}
	}
}
