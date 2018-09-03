package com.hallowizer.unscrew.coremod.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class UnscrewPlugin extends JavaPlugin {
	@Override
	public void onLoad() {
		getLogger().info("Unscrew has been loaded!");
	}
	
	@Override
	public void onEnable() {
		getLogger().info("Unscrew has been enabled!");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Unscrew has been disabled!");
	}
}
