package io.github.bananapuncher714.cartographer.core.api.configuration;

public class YamlKeyString extends YamlKey {
	String value;
	
	public YamlKeyString( String v ) {
		this.value = v;
	}
	
	@Override
	public YamlKey copyOf() {
		return new YamlKeyString( value );
	}
	
	@Override
	public String toString() {
		return '"' + value + '"';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		YamlKeyString other = (YamlKeyString) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
