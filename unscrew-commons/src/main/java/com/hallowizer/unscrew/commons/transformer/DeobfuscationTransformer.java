package com.hallowizer.unscrew.commons.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.hallowizer.unscrew.commons.deobfuscation.DeobfuscationClassRemapper;
import com.hallowizer.unscrew.commons.deobfuscation.DeobfuscationRemapper;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public final class DeobfuscationTransformer implements IClassTransformer, IClassNameTransformer {
	private final DeobfuscationRemapper remapper = new DeobfuscationRemapper();
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		DeobfuscationClassRemapper classRemapper = new DeobfuscationClassRemapper(cw, remapper);
		ClassReader cr = new ClassReader(basicClass);
		
		cr.accept(classRemapper, ClassReader.EXPAND_FRAMES);
		return cw.toByteArray();
	}
	
	@Override
	public String remapClassName(String name) {
		return remapper.map(name.replace('.', '/')).replace('/', '.');
	}
	
	@Override
	public String unmapClassName(String name) {
		return remapper.unmap(name.replace('.', '/')).replace('/', '.');
	}
}
