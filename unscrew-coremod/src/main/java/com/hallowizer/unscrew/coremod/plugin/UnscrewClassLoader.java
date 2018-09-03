package com.hallowizer.unscrew.coremod.plugin;

import org.bukkit.plugin.java.JavaPluginLoader;

public final class UnscrewClassLoader extends FilterClassLoader {
	public UnscrewClassLoader(JavaPluginLoader loader) throws Exception {
		super(loader);
	}
}
