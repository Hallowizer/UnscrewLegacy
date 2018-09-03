package com.hallowizer.unscrew.coremod.tweaker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class BootstrapTweaker implements ITweaker {
	private final List<String> args = new ArrayList<>();
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		this.args.addAll(args);
		this.args.addAll(Arrays.asList(
				"--version", "Unscrew",
				"--gameDir", gameDir.getAbsolutePath(),
				"--assetsDir", assetsDir.getAbsolutePath(),
				"--tweakClass", "com.hallowizer.unscrew.coremod.tweaker.PrimaryTweaker"
		));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@SneakyThrows
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.addTransformerExclusion("com.hallowizer.unscrew.coremod.transformer.BootstrapTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.BootstrapTransformer");
		
		Field classLoaderExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
		classLoaderExceptions.setAccessible(true);
		List<String> exclusions = (List<String>) classLoaderExceptions.get(classLoader);
		
		exclusions.remove("net.minecraft.launchwrapper."); // So we can fire it back at itself.
	}
	
	@Override
	public String getLaunchTarget() {
		return "net.minecraft.launchwrapper.Launch"; // Yes, fire the launch wrapper into itself.
	}
	
	@Override
	public String[] getLaunchArguments() {
		return (String[]) args.toArray(new String[args.size()]);
	}
}
