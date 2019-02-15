package io.github.bananapuncher714.cartographer.core.test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.bananapuncher714.cartographer.core.test.ChunkCache.Coord;

public class LoadSave {
	protected ExecutorService loadService = Executors.newFixedThreadPool( 2 );
	protected ExecutorService saveService = Executors.newFixedThreadPool( 2 );

	protected Map< Coord, Future< Boolean > > savingFuture = new HashMap< Coord, Future< Boolean > >();
	protected Map< Coord, Future< int[] > > loadingFuture = new ConcurrentHashMap< Coord, Future< int[] > >();
	
	protected ChunkCache cache;
	
	File saveDir;
	
	public LoadSave( ChunkCache cache, File saveDir ) {
		this.cache = cache;
		this.saveDir = saveDir;
		saveDir.mkdirs();
	}
	
	public void loadFuture( Coord coord ) {
		if ( cache.isGenerating( coord ) || savingFuture.containsKey( coord ) || loadingFuture.containsKey( coord ) ) {
			return;
		}
		loadingFuture.put( coord, loadService.submit( new LoadTask( getFileFor( coord ) ) ) );
	}
	
	public boolean saveFuture( Coord coord, int[] data ) {
		if ( savingFuture.containsKey( coord ) || loadingFuture.containsKey( coord ) ) {
			return false;
		}
		savingFuture.put( coord, saveService.submit( new SaveTask( getFileFor( coord ), data ) ) );
		return true;
	}
	
	public void update() {
		for ( Iterator< Entry< Coord, Future< Boolean > > > iterator = savingFuture.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< Coord, Future< Boolean > > entry = iterator.next();
			
			if ( entry.getValue().isDone() ) {
				iterator.remove();
			}
		}
		if ( savingFuture.size() > 0 ) {
			System.out.println( "Remaining: " + savingFuture.size() );
		}
		
		for ( Iterator< Entry< Coord, Future< int[] > > > iterator = loadingFuture.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< Coord, Future< int[] > > entry = iterator.next();
			
			if ( entry.getValue().isDone() ) {
				iterator.remove();
				
				Coord coord = entry.getKey();
				int[] value = null;
				try {
					value = entry.getValue().get();
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
				
				if ( value != null ) {
					cache.data.put( coord, value );
				} else {
					cache.generateCoord( coord );
				}
			}
		}
		if ( loadingFuture.size() > 0 ) {
			System.out.println( "Load remaining: " + loadingFuture.size() );
		}
	}
	
	protected File getFileFor( Coord coord ) {
		return new File( saveDir + "/" + coord.x + "_" + coord.y );
	}
}
