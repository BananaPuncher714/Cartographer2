package io.github.bananapuncher714.cartographer.core.test;

import java.io.File;
import java.util.concurrent.Callable;

import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class LoadTask implements Callable< int[] > {
	private final File load;
	
	public LoadTask( File file ) {
		load = file;
	}
	
	@Override
	public int[] call() throws Exception {
		if ( load.exists() ) {
			return FileUtil.readObject( int[].class, load );
		}
		return null;
	}
}
