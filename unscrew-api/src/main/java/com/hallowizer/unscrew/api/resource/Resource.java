package com.hallowizer.unscrew.api.resource;

import java.io.InputStream;
import java.io.Reader;

public interface Resource {
	public abstract String getName();
	public abstract String getSource();
	
	public abstract byte[] asByteArray();
	public abstract InputStream asInputStream();
	public abstract Reader asReader();
}
