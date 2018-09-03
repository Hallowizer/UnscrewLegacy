package com.hallowizer.unscrew.coremod.transformer;

import com.hallowizer.unscrew.coremod.preloader.resource.ResourceManager;

import net.minecraft.launchwrapper.IClassTransformer;

public final class ResourceClassTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return ResourceManager.transformClassAsResource(transformedName, basicClass);
	}
}
