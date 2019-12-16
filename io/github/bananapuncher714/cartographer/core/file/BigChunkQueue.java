package io.github.bananapuncher714.cartographer.core.file;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class BigChunkQueue {
	protected final ExecutorService savingService = Executors.newFixedThreadPool( 2 );
	protected final ExecutorService loadingService = Executors.newFixedThreadPool( 2 );
	
	protected Map< BigChunkLocation, Future< BigChunk > > loading = new ConcurrentHashMap< BigChunkLocation, Future< BigChunk > >();
	protected Map< BigChunkLocation, Future< Boolean > > saving = new ConcurrentHashMap< BigChunkLocation, Future< Boolean > >();
	
	protected MapDataCache cache;
	protected File saveLocation;
	
	public BigChunkQueue( File saveFile, MapDataCache cache ) {
		saveLocation = saveFile;
		this.cache = cache;
	}
	
	public boolean save( BigChunkLocation coord, BigChunk data ) {
		if ( saving.containsKey( coord ) || loading.containsKey( coord ) ) {
			return false;
		}
		saving.put( coord, savingService.submit( new TaskChunkSave( getFileFor( coord ), data ) ) );
		return true;
	}
	
	public boolean saveBlocking() {
		try {
			savingService.awaitTermination( 10, TimeUnit.MINUTES );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void load( BigChunkLocation coord ) {
		if ( saving.containsKey( coord ) || loading.containsKey( coord ) ) {
			return;
		}
		loading.put( coord, loadingService.submit( new TaskChunkLoad( getFileFor( coord ) ) ) );
	}
	
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
					ChunkData[] data = value.getData();
					
					for ( int index = 0; index < data.length; index++ ) {
						ChunkData chunkData = data[ index ];
						ChunkLocation location = new ChunkLocation( coord.getWorld(), relX + index % 16, relZ + index / 16 );
						if ( !cache.requiresGeneration( location ) ) {
							continue;
						}
						if ( chunkData == null ) {
							cache.addToProcessQueue( location );
						} else {
							cache.getData().put( location, chunkData );
						}
					}
				} else {
					for ( int x = 0; x < 16; x++ ) {
						for ( int z = 0; z < 16; z++ ) {
							ChunkLocation location = new ChunkLocation( coord.getWorld(), relX + x, relZ + z );
							if ( cache.requiresGeneration( location ) ) {
								cache.addToProcessQueue( location );
							}
						}
					}
				}
				iterator.remove();
			}
		}
	}
	
	protected File getFileFor( BigChunkLocation coord ) {
		return new File( saveLocation + "/" + coord.getWorld().getName() + "/" + coord.getX() + "/" + coord.getZ() );
	}
	
	protected class TaskChunkSave implements Callable< Boolean > {
		protected final File saveFile;
		protected final BigChunk chunk;
		
		TaskChunkSave( File saveFile, BigChunk chunk ) {
			this.saveFile = saveFile;
			this.chunk = chunk;
		}
		
		@Override
		public Boolean call() throws Exception {
			if ( chunk != null ) {
				FileUtil.writeObject( chunk, saveFile );
				return true;
			}
			return false;
		}
	}
	
	protected class TaskChunkLoad implements Callable< BigChunk > {
		protected final File file;
		
		TaskChunkLoad( File file ) {
			this.file = file;
		}
		
		@Override
		public BigChunk call() throws Exception {
			try {
				return FileUtil.readObject( BigChunk.class, file );
			} catch ( Exception exception ) {
				file.delete();
				return null;
			}
		}
	}
}
