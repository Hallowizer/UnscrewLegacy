package com.hallowizer.unscrew.coremod.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public final class BouncerTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("org.bukkit.craftbukkit.Main"))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, 0);
		
		for (MethodNode method : clazz.methods)
			for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> method.instructions.iterator())
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode cast = (MethodInsnNode) insn;
					
					if (cast.getOpcode() == Opcodes.INVOKESTATIC && cast.owner.equals("net/minecraft/server/MinecraftServer") && cast.name.equals("main") && cast.desc.equals("([Ljava/lang/String;)V"))
						cast.owner = "com/hallowizer/unscrew/coremod/main/Bouncer";
				}
		
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		return cw.toByteArray();
	}
}
