package com.hallowizer.unscrew.coremod.preloader.resource.url;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public final class TransformingStreamHandler extends URLStreamHandler {
	private final URL toTransform;
	private final URLStreamHandler handler;
	private final byte[] data;
	
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		if (!url.equals(toTransform))
			return openDelegateConnection(url);
		
		return new TransformingConnection(url, data);
	}
	
	@SneakyThrows
	private URLConnection openDelegateConnection(URL url) {
		Method openConnection = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
		openConnection.setAccessible(true);
		return (URLConnection) openConnection.invoke(handler, url);
	}
}
