package io.github.bananapuncher714.cartographer.core.test;

import java.io.File;
import java.util.concurrent.Callable;

import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class SaveTask implements Callable< Boolean > {
	private final File save;
	private final int[] data;
	
	public SaveTask( File saveFile, int[] data ) {
		save = saveFile;
		this.data = data;
	}
	
	@Override
	public Boolean call() throws Exception {
		FileUtil.writeObject( data, save );
		return true;
	}

}
