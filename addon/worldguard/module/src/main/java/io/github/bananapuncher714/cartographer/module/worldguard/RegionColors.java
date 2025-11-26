package io.github.bananapuncher714.cartographer.module.worldguard;

import java.awt.Color;

public class RegionColors {
	protected Color owner;
	protected Color member;
	protected Color nonMember;
	
	public RegionColors( Color owner, Color member, Color nonMember ) {
		this.owner = owner;
		this.member = member;
		this.nonMember = nonMember;
	}

	public Color getOwner() {
		return owner;
	}

	public RegionColors setOwner( Color owner ) {
		this.owner = owner;
		return this;
	}

	public Color getMember() {
		return member;
	}

	public RegionColors setMember( Color member ) {
		this.member = member;
		return this;
	}

	public Color getNonMember() {
		return nonMember;
	}

	public RegionColors setNonMember( Color nonMember ) {
		this.nonMember = nonMember;
		return this;
	}
}
