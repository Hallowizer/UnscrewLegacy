package com.hallowizer.unscrew.coremod.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public final class PluginLoadingTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("org.bukkit.plugin.java.JavaPluginLoader"))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, ClassReader.SKIP_FRAMES);
		
		for (MethodNode method : clazz.methods)
			if (method.name.equals("<init>")) {
				method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "org/bukkit/plugin/java/JavaPluginLoader", "loaders", "Ljava/util/List;"));
				method.instructions.add(new TypeInsnNode(Opcodes.NEW, "com/hallowizer/unscrew/coremod/plugin/UnscrewClassLoader"));
				method.instructions.add(new InsnNode(Opcodes.DUP));
				method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/hallowizer/unscrew/coremod/plugin/UnscrewClassLoader", "<init>", "(Lorg/bukkit/plugin/java/JavaPluginLoader;)V", false));
				method.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)V", true));
				method.instructions.add(new InsnNode(Opcodes.POP));
			} else if (method.name.equals("loadPlugin"))
				for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> method.instructions.iterator())
					if (insn instanceof TypeInsnNode) {
						TypeInsnNode cast = (TypeInsnNode) insn;
						
						if ((cast.getOpcode() == Opcodes.NEW || cast.getOpcode() == Opcodes.CHECKCAST) && cast.desc.equals("org/bukkit/plugin/java/PluginClassLoader"))
							cast.desc = "com/hallowizer/unscrew/coremod/plugin/FilterClassLoader";
					} else if (insn instanceof MethodInsnNode) {
						MethodInsnNode cast = (MethodInsnNode) insn;
						
						if (cast.getOpcode() == Opcodes.INVOKESPECIAL && cast.owner.equals("org/bukkit/plugin/java/PluginClassLoader") && cast.name.equals("<init>"))
							cast.owner = "com/hallowizer/unscrew/coremod/plugin/FilterClassLoader";
					}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		clazz.accept(cw);
		return cw.toByteArray();
	}
}
