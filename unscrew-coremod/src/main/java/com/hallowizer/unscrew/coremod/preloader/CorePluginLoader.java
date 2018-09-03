package com.hallowizer.unscrew.coremod.preloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.CharSource;
import com.hallowizer.unscrew.api.CorePlugin;
import com.hallowizer.unscrew.coremod.tweaker.CascadedTweaker;
import com.hallowizer.unscrew.coremod.tweaker.PluginInjectionTweaker;
import com.hallowizer.unscrew.coremod.tweaker.PluginInvocationTweaker;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

@UtilityClass
public class CorePluginLoader {
	@Getter
	private final Yaml yaml = new Yaml();
	private final List<PluginInvocationTweaker> corePlugins = new ArrayList<>();
	private final List<File> corePluginFiles = new ArrayList<>();
	private final Map<File,PluginInvocationTweaker> fileMap = new HashMap<>();
	private final Map<PluginInvocationTweaker,Integer> sortingMap = new HashMap<>();
	private final List<CharSource> accessTransformers = new ArrayList<>();
	@Getter
	@Setter
	private String bounceClass = "net.minecraft.server.Main";
	
	@SneakyThrows
	public void loadCorePlugins(LaunchClassLoader classLoader) {
		injectCascadedTweak(PluginInjectionTweaker.class);
		
		for (File file : new File("plugins").listFiles())
			if (!file.isDirectory() && file.getName().endsWith(".jar"))
				try (JarFile jar = new JarFile(file)) {
					JarEntry pluginYaml = jar.getJarEntry("plugin.yml");
					
					if (pluginYaml == null)
						continue;
					
					InputStream in = jar.getInputStream(pluginYaml);
					InputStreamReader rin = new InputStreamReader(in);
					
					Map<String,String> map = yaml.load(rin);
					String main = map.get("main");
					
					JarEntry mainEntry = jar.getJarEntry(main.replace('.', '/') + ".class");
					in = jar.getInputStream(mainEntry);
					try (BufferedInputStream bin = new BufferedInputStream(in)) {
						byte[] data = new byte[bin.available()];
						bin.read(data);
						
						ClassReader cr = new ClassReader(data);
						if (cr.getSuperName().equals("com/hallowizer/unscrew/api/CorePlugin"))
							loadCorePlugin(classLoader, file, map.get("name"), main);
					}
				}
		
		corePlugins.sort((tweak1, tweak2) -> sortingMap.get(tweak1)-sortingMap.get(tweak2));
	}
	
	@SuppressWarnings("unchecked")
	private void loadCorePlugin(LaunchClassLoader classLoader, File file, String name, String plugin) throws Exception {
		classLoader.addURL(file.toURI().toURL());
		
		Class<? extends CorePlugin> clazz = (Class<? extends CorePlugin>) classLoader.loadClass(plugin);
		CorePlugin corePlugin = clazz.newInstance();
		PluginInvocationTweaker tweak = new PluginInvocationTweaker(name, corePlugin, file);
		
		corePlugins.add(tweak);
		corePluginFiles.add(file);
		fileMap.put(file, tweak);
		sortingMap.put(tweak, corePlugin.getSortingIndex());
	}
	
	@SuppressWarnings("unchecked")
	public void injectCascadedTweak(Class<? extends CascadedTweaker> tweak) {
		((List<String>) Launch.blackboard.get("TweakClasses")).add(tweak.getName());
	}
	
	@SuppressWarnings("unchecked")
	public void injectCorePlugins() {
		((List<ITweaker>) Launch.blackboard.get("Tweaks")).addAll(corePlugins);
	}
	
	public void registerAccessTransformer(CharSource accessTransformer) {
		accessTransformers.add(accessTransformer);
	}
	
	public Iterable<CharSource> getAccessTransformers() {
		return () -> accessTransformers.iterator();
	}
	
	public Iterable<File> getCorePluginFiles() {
		return () -> corePluginFiles.iterator();
	}
	
	public CorePlugin getCorePlugin(File file) {
		return fileMap.get(file).getPlugin();
	}
}
