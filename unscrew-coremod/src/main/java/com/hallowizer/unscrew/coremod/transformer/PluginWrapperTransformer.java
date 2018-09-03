package com.hallowizer.unscrew.coremod.transformer;

import com.hallowizer.unscrew.api.TransformerException;

import lombok.RequiredArgsConstructor;
import net.minecraft.launchwrapper.IClassTransformer;

@RequiredArgsConstructor
public final class PluginWrapperTransformer implements IClassTransformer {
	private final String plugin;
	private final IClassTransformer transformer;
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			return transformer.transform(transformedName, transformedName, basicClass); // Simplify life for them by giving them the deobfuscated name.
		} catch (TransformerException e) {
			throw e;
		} catch (Exception e) {
			throw new TransformerException(plugin + "'s transformer " + transformer.getClass().getName() + " failed to transform the class " + transformedName + ". Please contact the author if this problem persists.", e);
		}
	}
}
