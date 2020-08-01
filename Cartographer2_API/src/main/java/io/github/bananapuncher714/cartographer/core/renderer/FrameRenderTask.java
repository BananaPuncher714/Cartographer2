package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.concurrent.RecursiveAction;

public class FrameRenderTask extends RecursiveAction {
	protected RenderInfo info;
	
	protected FrameRenderTask( RenderInfo info ) {
		this.info = info;
	}
	
	@Override
	protected void compute() {
	}
}
