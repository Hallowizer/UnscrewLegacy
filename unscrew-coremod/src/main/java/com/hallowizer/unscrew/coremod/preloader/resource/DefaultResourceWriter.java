package com.hallowizer.unscrew.coremod.preloader.resource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.hallowizer.unscrew.api.resource.Resource;
import com.hallowizer.unscrew.api.resource.ResourceWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultResourceWriter implements ResourceWriter {
	private final String name;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final String source;
	
	@Override
	public OutputStream asOutputStream() {
		return out;
	}
	
	@Override
	public Writer asWriter() {
		return new OutputStreamWriter(out);
	}
	
	@Override
	public Resource create() {
		return new DefaultResource(name, source, out.toByteArray());
	}
}
