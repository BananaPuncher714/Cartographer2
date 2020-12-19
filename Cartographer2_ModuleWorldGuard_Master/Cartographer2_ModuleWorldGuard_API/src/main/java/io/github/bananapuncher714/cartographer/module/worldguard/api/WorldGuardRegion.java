package io.github.bananapuncher714.cartographer.module.worldguard.api;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class WorldGuardRegion {
	protected String name;
	protected Set< UUID > members = new HashSet< UUID >();
	protected Set< UUID > owners = new HashSet< UUID >();
	
	public WorldGuardRegion( String name ) {
		this.name = name;
	}
	
	public Set< UUID > getMembers() {
		return members;
	}

	public Set< UUID > getOwners() {
		return owners;
	}

	public String getName() {
		return name;
	}
}
