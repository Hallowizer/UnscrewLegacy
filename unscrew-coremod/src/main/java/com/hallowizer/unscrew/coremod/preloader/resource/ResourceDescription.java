package com.hallowizer.unscrew.coremod.preloader.resource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResourceDescription {
	private final String name;
	private final String source;
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ResourceDescription))
			return false;
		
		ResourceDescription description = (ResourceDescription) other;
		return name.equals(description.name) && source.equals(description.source);
	}
}
