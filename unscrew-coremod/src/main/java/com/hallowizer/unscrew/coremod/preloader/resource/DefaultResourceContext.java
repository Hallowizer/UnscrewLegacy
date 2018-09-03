package com.hallowizer.unscrew.coremod.preloader.resource;

import com.hallowizer.unscrew.api.resource.Resource;
import com.hallowizer.unscrew.api.resource.ResourceContext;
import com.hallowizer.unscrew.api.resource.ResourceWriter;

import lombok.Setter;

public final class DefaultResourceContext implements ResourceContext {
	@Setter
	private String source;
	
	@Override
	public ResourceWriter newWriter(String resource) {
		return new DefaultResourceWriter(resource, source);
	}
	
	@Override
	public Resource newResource(String name, byte[] data) {
		return new DefaultResource(name, source, data);
	}
	
	@Override
	public Resource newResource(String name, String data) {
		return newResource(name, data.getBytes());
	}
}
