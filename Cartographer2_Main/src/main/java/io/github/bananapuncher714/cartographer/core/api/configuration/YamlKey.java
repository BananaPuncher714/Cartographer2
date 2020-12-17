package io.github.bananapuncher714.cartographer.core.api.configuration;

public abstract class YamlKey {
	public abstract YamlKey copyOf();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( Object obj );
}
