package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.HashSet;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;

public class SubRenderInfo {
	protected byte[] data;
	protected int index;
	protected Set< BigChunkLocation > requiresRender = new HashSet< BigChunkLocation >();
}
