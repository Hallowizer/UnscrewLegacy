package com.hallowizer.unscrew.commons.transformer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

public abstract class CoreAccessTransformer extends AccessTransformer {
	public CoreAccessTransformer(String atFile) throws Exception {
		InputStream in = getClass().getResourceAsStream(atFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteStreams.copy(in, out);
		
		ByteSource bytes = ByteSource.wrap(out.toByteArray());
		readMappings(bytes.asCharSource(Charsets.UTF_8));
	}
}
