package com.hallowizer.unscrew.coremod.tweaker;

import java.io.File;
import java.util.List;

import com.hallowizer.unscrew.coremod.preloader.CoreInjection;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class PrimaryTweaker implements ITweaker {
	private List<String> args;
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		this.args = args;
	}
	
	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		CoreInjection.inject(classLoader);
	}
	
	@Override
	public String getLaunchTarget() {
		return "org.bukkit.craftbukkit.Main";
	}
	
	@Override
	public String[] getLaunchArguments() {
		return (String[]) args.toArray(new String[args.size()]);
	}
}
