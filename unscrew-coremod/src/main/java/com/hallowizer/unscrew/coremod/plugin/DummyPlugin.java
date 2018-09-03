package com.hallowizer.unscrew.coremod.plugin;

import java.lang.reflect.Field;

import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.SneakyThrows;

public final class DummyPlugin extends JavaPlugin {
	static String delegatePlugin;
	static FilterClassLoader classLoader;
	
	@Getter
	private JavaPlugin mainPlugin;
	@Getter
	private String mainPluginClass;
	private FilterClassLoader currentClassLoader;
	
	public DummyPlugin() {
		mainPluginClass = delegatePlugin;
		currentClassLoader = classLoader;
		
		currentClassLoader.initDummyPlugin(this);
	}
	
	@SneakyThrows
	void init(JavaPlugin plugin) {
		mainPlugin = plugin;
		
		Field description = JavaPlugin.class.getDeclaredField("description");
		description.setAccessible(true);
		description.set(mainPlugin, description.get(this));
	}
	
	@Override
	public void onLoad() {
		mainPlugin.onLoad();
	}
	
	@Override
	public void onEnable() {
		mainPlugin.onEnable();
	}
	
	@Override
	public void onDisable() {
		mainPlugin.onDisable();
	}
}
