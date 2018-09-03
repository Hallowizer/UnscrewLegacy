package com.hallowizer.unscrew.coremod.transformer;

import com.google.common.io.CharSource;
import com.hallowizer.unscrew.commons.transformer.AccessTransformer;
import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;

public final class PluginAccessTransformer extends AccessTransformer {
	public PluginAccessTransformer() {
		for (CharSource accessTransformer : CorePluginLoader.getAccessTransformers())
			readMappings(accessTransformer);
	}
}
