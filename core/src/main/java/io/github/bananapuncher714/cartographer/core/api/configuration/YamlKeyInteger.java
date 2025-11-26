package io.github.bananapuncher714.cartographer.core.api.configuration;

public class YamlKeyInteger extends YamlKey {
	int index;

	public YamlKeyInteger( int v ) {
		index = v;
	}
	
	@Override
	public YamlKey copyOf() {
		return new YamlKeyInteger( index );
	}
	
	@Override
	public String toString() {
		return String.valueOf( index );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YamlKeyInteger other = (YamlKeyInteger) obj;
		if (index != other.index)
			return false;
		return true;
	}
}
