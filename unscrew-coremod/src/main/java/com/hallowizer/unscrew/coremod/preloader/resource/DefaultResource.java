package com.hallowizer.unscrew.coremod.preloader.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.hallowizer.unscrew.api.resource.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultResource implements Resource {
	@Getter
	private final String name;
	@Getter
	private final String source;
	private final byte[] data;
	
	@Override
	public byte[] asByteArray() {
		return data;
	}
	
	@Override
	public InputStream asInputStream() {
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public Reader asReader() {
		return new InputStreamReader(asInputStream());
	}
}
