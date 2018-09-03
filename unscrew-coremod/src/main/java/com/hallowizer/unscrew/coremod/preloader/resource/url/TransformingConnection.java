package com.hallowizer.unscrew.coremod.preloader.resource.url;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

public final class TransformingConnection extends URLConnection {
	private final byte[] data;
	
	public TransformingConnection(URL url, byte[] data) {
		super(url);
		this.data = data;
	}
	
	@Override
	public void connect() throws IOException {
		// NOOP
	}
	
	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public Permission getPermission() {
		return null;
	}
}
