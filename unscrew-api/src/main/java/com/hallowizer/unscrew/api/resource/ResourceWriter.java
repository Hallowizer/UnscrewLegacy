package com.hallowizer.unscrew.api.resource;

import java.io.OutputStream;
import java.io.Writer;

public interface ResourceWriter {
	public abstract OutputStream asOutputStream();
	public abstract Writer asWriter();
	
	public abstract Resource create();
}
