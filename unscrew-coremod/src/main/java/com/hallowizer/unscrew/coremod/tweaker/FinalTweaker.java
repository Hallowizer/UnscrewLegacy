package com.hallowizer.unscrew.coremod.tweaker;

import java.io.File;
import java.util.List;

import com.hallowizer.unscrew.coremod.preloader.CoreInjection;

import net.minecraft.launchwrapper.LaunchClassLoader;

public final class FinalTweaker extends CascadedTweaker {
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		// NOOP
	}
	
	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		CoreInjection.injectFinalTransformers(classLoader);
	}
	
	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}
}
