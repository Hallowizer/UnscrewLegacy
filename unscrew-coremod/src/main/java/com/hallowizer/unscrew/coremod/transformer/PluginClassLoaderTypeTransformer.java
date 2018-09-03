package com.hallowizer.unscrew.coremod.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public final class PluginClassLoaderTypeTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("com.hallowizer.unscrew.coremod.plugin.FilterClassLoader"))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, ClassReader.SKIP_FRAMES);
		
		clazz.superName = "org/bukkit/plugin/java/PluginClassLoader";
		
		for (MethodNode method : clazz.methods)
			if (method.name.equals("<init>") && method.desc.equals("(Lorg/bukkit/plugin/java/JavaPluginLoader;Ljava/lang/ClassLoader;Lorg/bukkit/plugin/PluginDescriptionFile;Ljava/io/File;Ljava/io/File;)V")) {
				InsnList insns = new InsnList();
				
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 4));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 5));
				insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "org/bukkit/plugin/java/PluginClassLoader", "<init>", "(Lorg/bukkit/plugin/java/JavaPluginLoader;Ljava/lang/ClassLoader;Lorg/bukkit/plugin/PluginDescriptionFile;Ljava/io/File;Ljava/io/File;)V", false));
				
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 5));
				insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/hallowizer/unscrew/coremod/plugin/FilterClassLoader", "constructorCallback", "(Ljava/io/File;)V", false));
				
				method.instructions = insns;
			}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		clazz.accept(cw);
		return cw.toByteArray();
	}
}
