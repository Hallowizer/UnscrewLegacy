package com.hallowizer.unscrew.api.resource;

public interface ResourceContext {
	public abstract ResourceWriter newWriter(String resource);
	
	public abstract Resource newResource(String name, byte[] data);
	public abstract Resource newResource(String name, String data);
}
