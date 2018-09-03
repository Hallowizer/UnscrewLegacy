package com.hallowizer.unscrew.commons.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.IClassTransformer;

public abstract class AccessTransformer implements IClassTransformer {
	private final Multimap<String,Modifier> modifiers = ArrayListMultimap.create();
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!modifiers.containsKey(name))
			return basicClass;
		
		ClassNode clazz = new ClassNode();
		new ClassReader(basicClass).accept(clazz, 0);
		
		Collection<Modifier> modifiers = this.modifiers.get(name);
		for (Modifier modifier : modifiers)
			modifier.changeAccess(clazz);
		
		ClassWriter cw = new ClassWriter(0);
		clazz.accept(cw);
		return cw.toByteArray();
	}
	
	@SneakyThrows
	protected void readMappings(CharSource source) {
		source.readLines(new LineProcessor<String>() {
			@Override
			public boolean processLine(String input) throws IOException {
				String line = input.split("#")[0].trim();
				
				if (line.length() == 0)
					return true;
				
				List<String> parts = Lists.newArrayList(Splitter.on(" ").trimResults().split(line));
				if (parts.size() > 3)
					throw new RuntimeException("Invalid access transformer line: " + line);
				
				Modifier modifier = new Modifier();
				modifier.setTargetAccess(parts.get(0));
				
				if (parts.size() == 2)
					modifier.modifyClassVisibility = true;
				else {
					String name = parts.get(2);
					int descriptorStart = name.indexOf('(');
					
					if (descriptorStart > 0) {
						modifier.name = name.substring(0, descriptorStart);
						modifier.descriptor = name.substring(descriptorStart+1);
					} else
						modifier.name = name;
				}
				
				String clazz = parts.get(1).replace('/', '.');
				modifiers.put(clazz, modifier);
				return true;
			}
			
			@Override
			public String getResult() {
				return "EAT YUMMY PIES FOR THE REST OF YOUR LIFE";
			}
		});
	}
	
	private final class Modifier {
		private String name = "";
		private String descriptor = "";
		private int oldAccess = 0;
		private int newAccess = 0;
		private int targetAccess = 0;
		private boolean changeFinal = false;
		private boolean markFinal = false;
		private boolean modifyClassVisibility = false;
		
		private void setTargetAccess(String name) {
			if (name.startsWith("public"))
				targetAccess = Opcodes.ACC_PUBLIC;
			else if (name.startsWith("private"))
				targetAccess = Opcodes.ACC_PRIVATE;
			else if (name.startsWith("protected"))
				targetAccess = Opcodes.ACC_PROTECTED;
			
			if (name.endsWith("-f")) {
				changeFinal = true;
				markFinal = false;
			} else if (name.endsWith("+f")) {
				changeFinal = true;
				markFinal = true;
			}
		}
		
		private void changeAccess(ClassNode clazz) {
			List<MethodDescription> removeInvokeSpecial = new ArrayList<>();
			List<MethodDescription> addInvokeSpecial = new ArrayList<>();
			
			if (modifyClassVisibility)
				changeClassAccess(clazz);
			else {
				if (descriptor.isEmpty())
					for (FieldNode field : clazz.fields)
						changeFieldAccess(field);
				else
					for (MethodNode method : clazz.methods)
						changeMethodAccess(clazz.name, method, removeInvokeSpecial, addInvokeSpecial);
			}
			
			for (MethodNode method : clazz.methods)
				updateInvokeSpecial(method, removeInvokeSpecial, addInvokeSpecial);
		}
		
		private void changeClassAccess(ClassNode clazz) {
			clazz.access = transformAccess(clazz.access);
		}
		
		private void changeFieldAccess(FieldNode field) {
			if (field.name.equals(name) || name.equals("*"))
				field.access = transformAccess(field.access);
		}
		
		private void changeMethodAccess(String owner, MethodNode method, List<MethodDescription> removeInvokeSpecial, List<MethodDescription> addInvokeSpecial) {
			if (method.name.equals("*") || (method.name.equals(name) && method.desc.equals(descriptor))) {
				method.access = transformAccess(method.access);
				
				if (!method.name.equals("<init>")) {
					boolean privateBefore = (oldAccess & Opcodes.ACC_PRIVATE) != 0;
					boolean privateAfter = (newAccess & Opcodes.ACC_PRIVATE) != 0;
					
					if (privateBefore && !privateAfter)
						removeInvokeSpecial.add(new MethodDescription(owner, method));
					else if (privateAfter && !privateBefore)
						addInvokeSpecial.add(new MethodDescription(owner, method));
				}
			}
		}
		
		private int transformAccess(int access) {
			oldAccess = access;
			int value = (access & ~7);
			
			switch (access & 7) {
			case Opcodes.ACC_PRIVATE:
				value |= targetAccess;
				break;
			case 0:
				value |= (targetAccess != Opcodes.ACC_PRIVATE ? targetAccess : 0);
				break;
			case Opcodes.ACC_PROTECTED:
				value |= (targetAccess != Opcodes.ACC_PRIVATE && targetAccess != 0 ? targetAccess : Opcodes.ACC_PROTECTED);
				break;
			case Opcodes.ACC_PUBLIC:
				value |= (targetAccess != Opcodes.ACC_PRIVATE && targetAccess != 0 && targetAccess != Opcodes.ACC_PROTECTED ? targetAccess : Opcodes.ACC_PUBLIC);
				break;
			}
			
			if (changeFinal) {
				if (markFinal)
					value |= Opcodes.ACC_FINAL;
				else
					value &= ~Opcodes.ACC_FINAL;
			}
			
			newAccess = value;
			return value;
		}
		
		private void updateInvokeSpecial(final MethodNode method, List<MethodDescription> removeInvokeSpecial, List<MethodDescription> addInvokeSpecial) {
			for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> method.instructions.iterator())
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode cast = (MethodInsnNode) insn;
					MethodDescription description = new MethodDescription(cast);
					
					if (cast.getOpcode() == Opcodes.INVOKESPECIAL && removeInvokeSpecial.contains(description))
						cast.setOpcode(Opcodes.INVOKEVIRTUAL);
					else if (cast.getOpcode() == Opcodes.INVOKEVIRTUAL && addInvokeSpecial.contains(description))
						cast.setOpcode(Opcodes.INVOKESPECIAL);
				}
		}
	}
	
	private final class MethodDescription {
		private final String owner;
		private final String name;
		private final String descriptor;
		
		private MethodDescription(String owner, MethodNode method) {
			this.owner = owner;
			name = method.name;
			descriptor = method.desc;
		}
		
		private MethodDescription(MethodInsnNode insn) {
			owner = insn.owner;
			name = insn.name;
			descriptor = insn.desc;
		}
		
		@Override
		public boolean equals(Object uncast) {
			if (!(uncast instanceof MethodDescription))
				return false;
			
			MethodDescription other = (MethodDescription) uncast;
			return other.owner.equals(owner) && other.name.equals(name) && other.descriptor.equals(descriptor);
		}
	}
}
