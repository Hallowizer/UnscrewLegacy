package com.hallowizer.unscrew.coremod.preloader;

import com.hallowizer.unscrew.commons.deobfuscation.DeobfuscationData;

import lombok.experimental.UtilityClass;
import net.minecraft.launchwrapper.LaunchClassLoader;

@UtilityClass
public class CoreInjection {
	public void inject(LaunchClassLoader classLoader) {
		addExclusions(classLoader);
		injectStartTransformers(classLoader);
		CorePluginLoader.loadCorePlugins(classLoader);
	}
	
	private void addExclusions(LaunchClassLoader classLoader) {
		classLoader.addClassLoaderExclusion("com.hallowizer.unscrew.coremod.preloader.");
		classLoader.addClassLoaderExclusion("com.hallowizer.unscrew.coremod.transformer.BootstrapTransformer");
		classLoader.addClassLoaderExclusion("com.hallowizer.unscrew.coremod.tweaker.PluginInvocationTweaker");
		classLoader.addClassLoaderExclusion("com.google.guava.");
		classLoader.addClassLoaderExclusion("org.yaml.snakeyaml.");
		classLoader.addClassLoaderExclusion("org.objectweb.asm.");
		
		classLoader.addTransformerExclusion("com.hallowizer.unscrew.coremod.transformer.");
		classLoader.addTransformerExclusion("com.hallowizer.unscrew.commons.transformer.");
		classLoader.addTransformerExclusion("com.hallowizer.unscrew.commons.deobfuscation.");
	}
	
	private void injectStartTransformers(LaunchClassLoader classLoader) {
		DeobfuscationData.injectDeobfuscationTransformer(classLoader);
		classLoader.registerTransformer("com.hallowizer.unscrew.commons.transformer.StartAccessTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.ResourceClassTransformer");
	}
	
	public void injectFinalTransformers(LaunchClassLoader classLoader) {
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.PluginAccessTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.FinalAccessTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.ResourceReadingTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.ResourcePluginYamlTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.PluginLoadingTransformer");
		classLoader.registerTransformer("com.hallowizer.unscrew.coremod.transformer.BouncerTransformer");
	}
}
