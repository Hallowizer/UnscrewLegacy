package com.hallowizer.unscrew.coremod.plugin;

import java.io.File;
import java.lang.reflect.Field;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.google.common.collect.Iterables;
import com.hallowizer.unscrew.api.CorePlugin;
import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class FilterClassLoader {
	private boolean initialized;
	private DummyPlugin dummy;
	
	public FilterClassLoader(JavaPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file) {
		// Transform this method in the transformer.
	}
	
	public FilterClassLoader(JavaPluginLoader loader) throws Exception { // This constructor is used by UnscrewClassLoader.
		this(loader,
			
				FilterClassLoader.class.getClassLoader() // LaunchClassLoader
						.getClass().getClassLoader(), // URLClassLoader
			
			new PluginDescriptionFile(FilterClassLoader.class.getResourceAsStream("/unscrewPlugin.yml")),
			new File("Unscrew"),
			null
		);
	}
	
	@SuppressWarnings("unused") // This is invoked in the bytecode.
	private void constructorCallback(File file) throws Exception {
		if (Iterables.contains(CorePluginLoader.getCorePluginFiles(), file)) {
			reparseCorePlugin(CorePluginLoader.getCorePlugin(file));
			return;
		}
		
		LaunchClassLoader classLoader = (LaunchClassLoader) getClass().getClassLoader();
		classLoader.addURL(file.toURI().toURL());
		initCommon((JavaPlugin) findClass(dummy.getMainPluginClass(), false).getDeclaredConstructor().newInstance());
		initialized = true;
	}
	
	private void reparseCorePlugin(CorePlugin plugin) {
		initCommon(plugin.getJavaPlugin());
	}
	
	@SneakyThrows
	private void initCommon(JavaPlugin plugin) {
		dummy.init(plugin);
		
		Field field = Class.forName("org.bukkit.plugin.java.PluginClassLoader").getDeclaredField("plugin");
		field.setAccessible(true);
		field.set(this, dummy.getMainPlugin());
	}
	
	void initDummyPlugin(DummyPlugin plugin) {
		dummy = plugin;
	}
	
	// No @Override because the compiler thinks this extends Object.
	public synchronized Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
		if (!initialized) {
			DummyPlugin.delegatePlugin = name;
			DummyPlugin.classLoader = this;
			return DummyPlugin.class;
		}
		
		return getClass().getClassLoader().loadClass(name);
	}
}
