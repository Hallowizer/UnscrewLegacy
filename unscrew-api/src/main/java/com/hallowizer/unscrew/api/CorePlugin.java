package com.hallowizer.unscrew.api;

import org.bukkit.plugin.java.JavaPlugin;

import com.hallowizer.unscrew.api.resource.IResourceTransformer;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;

public abstract class CorePlugin {
	public IClassTransformer[] getClassTransformers() {
		return new IClassTransformer[0];
	}
	
	public IResourceTransformer[] getResourceTransformers() {
		return new IResourceTransformer[0];
	}
	
	public String getAccessTransformerFile() {
		return null;
	}
	
	public ITweaker getTweak() {
		return null;
	}
	
	public int getSortingIndex() {
		return 0;
	}
	
	public String getAlternateMain() {
		return null;
	}
	
	public JavaPlugin getJavaPlugin() {
		return null;
	}
}
