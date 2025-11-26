package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.concurrent.RecursiveTask;

public class SubRenderTask extends RecursiveTask<SubRenderInfo> {
	protected RenderInfo info;
	protected int index;
	protected int length;

	protected SubRenderTask( RenderInfo info, int index, int length ) {
	}

	@Override
	protected SubRenderInfo compute() {
		return null;
	}
}
