package com.hallowizer.unscrew.coremod.tweaker;

import java.io.File;
import java.util.List;

import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;

import net.minecraft.launchwrapper.LaunchClassLoader;

public final class PluginInjectionTweaker extends CascadedTweaker {
	public PluginInjectionTweaker() {
		CorePluginLoader.injectCorePlugins();
	}
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		// NOOP
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		CorePluginLoader.injectCascadedTweak(FinalTweaker.class);
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}
}
