package com.hallowizer.unscrew.coremod.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public final class ResourceReadingTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, 0);
		
		for (MethodNode method : clazz.methods)
			for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> method.instructions.iterator())
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode cast = (MethodInsnNode) insn;
					
					if (cast.owner.equals("java/lang/Class")) {
						if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.name.equals("getResource") && cast.desc.equals("(Ljava/lang/String;)Ljava/net/URL;")) {
							cast.setOpcode(Opcodes.INVOKESTATIC);
							cast.owner = "com/hallowizer/unscrew/coremod/preloader/resource/ResourceManager";
							cast.name = "getResource";
							cast.desc = "(Ljava/lang/Class;Ljava/lang/String;)Ljava/net/URL;";
						}
						
						if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.name.equals("getResourceAsStream") && cast.desc.equals("(Ljava/lang/String;)Ljava/io/InputStream;")) {
							cast.setOpcode(Opcodes.INVOKESTATIC);
							cast.owner = "com/hallowizer/unscrew/coremod/preloader/resource/ResourceManager";
							cast.name = "getResourceAsStream";
							cast.desc = "(Ljava/lang/Class;Ljava/lang/String;)Ljava/io/InputStream;";
						}
					}
					
					if (cast.owner.equals("java/lang/ClassLoader")) {
						if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.name.equals("getResource") && cast.desc.equals("(Ljava/lang/String;)Ljava/net/URL;")) {
							cast.setOpcode(Opcodes.INVOKESTATIC);
							cast.owner = "com/hallowizer/unscrew/coremod/preloader/resource/ResourceManager";
							cast.name = "getResource";
							cast.desc = "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/net/URL;";
						}
						
						if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.name.equals("getResourceAsStream") && cast.desc.equals("(Ljava/lang/String;)Ljava/io/InputStream;")) {
							cast.setOpcode(Opcodes.INVOKESTATIC);
							cast.owner = "com/hallowizer/unscrew/coremod/preloader/resource/ResourceManager";
							cast.name = "getResourceAsStream";
							cast.desc = "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/io/InputStream;";
						}
					}
				}
		
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		return cw.toByteArray();
	}
}
