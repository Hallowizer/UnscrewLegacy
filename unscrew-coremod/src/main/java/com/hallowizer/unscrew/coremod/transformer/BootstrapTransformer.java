package com.hallowizer.unscrew.coremod.transformer;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class BootstrapTransformer implements IClassTransformer {
	public static final ThreadLocal<URL> currentURL = new ThreadLocal<URL>();
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.equals("net/minecraft/launchwrapper/LaunchClassLoader"))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, 0);
		
		MethodNode findClass = null;
		
		for (MethodNode method : clazz.methods)
			if (method.name.equals("findClass") && method.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;"))
				findClass = method;
		
		if (findClass == null)
			return basicClass;
		
		final MethodNode finalFindClass = findClass;
		
		for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> finalFindClass.instructions.iterator())
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode cast = (MethodInsnNode) insn;
				
				if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && cast.owner.equals("net/minecraft/launchwrapper/LaunchClassLoader") && cast.name.equals("findCodeSourceConnectionFor") && cast.desc.equals("(Ljava/lang/String;)Ljava/net/URLConnection;")) {
					cast.setOpcode(Opcodes.INVOKESTATIC);
					cast.owner = "com/hallowizer/unscrew/coremod/transformer/BootstrapTransformer";
					cast.name = "inject";
					cast.desc = "(Lnet/minecraft/launchwrapper/LaunchClassLoader;Ljava/lang/String;)Ljava/net/URLConnection;";
				}
			}
		
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		return cw.toByteArray();
	}
	
	public static URLConnection inject(LaunchClassLoader classLoader, String name) throws Exception {
		Method findCodeSourceConnectionFor = classLoader.getClass().getDeclaredMethod("findCodeSourceConnectionFor", String.class);
		findCodeSourceConnectionFor.setAccessible(true);
		URLConnection connection = (URLConnection) findCodeSourceConnectionFor.invoke(classLoader, name);
		
		currentURL.set(connection.getURL());
		return connection;
	}
}
