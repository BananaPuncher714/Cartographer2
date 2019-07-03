package io.github.bananapuncher714.cartographer.core.util;

import org.bukkit.Material;

public class CrossVersionMaterial {
	public final Material material;
	public final int durability;
	
	public CrossVersionMaterial( Material material ) {
		this( material, 0 );
	}
	
	public CrossVersionMaterial( Material material, int durability ) {
		this.material = material;
		this.durability = durability;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + durability;
		result = prime * result + ((material == null) ? 0 : material.hashCode());
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
		CrossVersionMaterial other = (CrossVersionMaterial) obj;
		if (durability != other.durability)
			return false;
		if (material != other.material)
			return false;
		return true;
	}
}
