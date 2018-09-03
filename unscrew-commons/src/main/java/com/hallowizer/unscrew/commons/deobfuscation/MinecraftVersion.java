package com.hallowizer.unscrew.commons.deobfuscation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MinecraftVersion implements Comparable<MinecraftVersion> {
	private final String name;
	private final boolean unique;
	
	@Override
	public int compareTo(MinecraftVersion other) {
		String name = this.name;
		String otherName = other.name;
		
		if (name.split(".").length == 2)
			name += ".0";
		
		if (otherName.split(".").length == 2)
			otherName += ".0";
		
		for (int i = 0; i < 3; i++) {
			int compare = Integer.parseInt(name.split(".")[i])-Integer.parseInt(otherName.split(".")[i]);
			if (compare != 0)
				return compare;
		}
		
		return 0;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof MinecraftVersion && !unique && !((MinecraftVersion) other).unique ? ((MinecraftVersion) other).name.equals(name) : this == other;
	}
	
	@Override
	public int hashCode() {
		return unique ? System.identityHashCode(this) : name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
