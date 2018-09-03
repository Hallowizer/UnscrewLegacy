package com.hallowizer.unscrew.coremod.tweaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.hallowizer.unscrew.api.CorePlugin;
import com.hallowizer.unscrew.api.resource.IResourceTransformer;
import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;
import com.hallowizer.unscrew.coremod.preloader.resource.ResourceManager;
import com.hallowizer.unscrew.coremod.transformer.PluginWrapperTransformer;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class PluginInvocationTweaker extends CascadedTweaker {
	private final String name;
	@Getter
	private final CorePlugin plugin;
	private final ITweaker tweak;
	private final File file;
	
	public PluginInvocationTweaker(String name, CorePlugin plugin, File file) {
		this.name = name;
		this.plugin = plugin;
		this.tweak = plugin.getTweak();
		this.file = file;
	}
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		if (tweak != null)
			tweak.acceptOptions(args, gameDir, assetsDir, profile);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@SneakyThrows
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		Field transformers = classLoader.getClass().getDeclaredField("transformers");
		transformers.setAccessible(true);
		List<IClassTransformer> transformerList = (List<IClassTransformer>) transformers.get(classLoader);
		
		for (IClassTransformer transformer : plugin.getClassTransformers())
			transformerList.add(new PluginWrapperTransformer(name, transformer));
		
		for (IResourceTransformer transformer : plugin.getResourceTransformers())
			ResourceManager.registerTransformer(name, transformer);
		
		String accessTransformer = plugin.getAccessTransformerFile();
		if (accessTransformer != null) {
			try (JarFile jar = new JarFile(file)) {
				JarEntry entry = jar.getJarEntry(accessTransformer);
				InputStream in = jar.getInputStream(entry);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ByteStreams.copy(in, out);
				
				ByteSource bytes = ByteSource.wrap(out.toByteArray());
				CorePluginLoader.registerAccessTransformer(bytes.asCharSource(Charsets.UTF_8));
			}
		}
		
		String main = plugin.getAlternateMain();
		if (main != null && CorePluginLoader.getBounceClass() != null)
			CorePluginLoader.setBounceClass(main);
		
		if (tweak != null)
			tweak.injectIntoClassLoader(classLoader);
	}
	
	@Override
	public String[] getLaunchArguments() {
		return tweak == null ? new String[0] : tweak.getLaunchArguments();
	}
}
