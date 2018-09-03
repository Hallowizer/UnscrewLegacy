package com.hallowizer.unscrew.coremod.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public final class ResourcePluginYamlTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("org.bukkit.plugin.java.JavaPluginLoader"))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, 0);
		
		MethodNode getPluginDescription = null;
		
		for (MethodNode method : clazz.methods)
			if (method.name.equals("getPluginDescription") && method.desc.equals("(Ljava/io/File;)Lorg/bukkit/plugin/PluginDescriptionFile;"))
				getPluginDescription = method;
		
		if (getPluginDescription == null)
			return basicClass;
		
		final MethodNode finalMethod = getPluginDescription;
		for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> finalMethod.instructions.iterator())
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode cast = (MethodInsnNode) insn;
				
				if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.owner.equals("java/util/jar/JarFile") && cast.name.equals("getInputStream") && cast.desc.equals("(Ljava/util/jar/JarEntry;)Ljava/io/InputStream;")) {
					cast.setOpcode(Opcodes.INVOKESTATIC);
					cast.owner = "com/hallowizer/unscrew/coremod/preloader/resource/ResourceManager";
					cast.name = "getTransformedPluginYaml";
					cast.desc = "(Ljava/util/jar/JarFile;Ljava/util/jar/JarEntry;)Ljava/io/InputStream";
				}
			}
		
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		return cw.toByteArray();
	}
}
