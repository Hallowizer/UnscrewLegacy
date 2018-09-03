package com.hallowizer.unscrew.commons.deobfuscation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class DeobfuscationRemapper extends Remapper {
	private final LaunchClassLoader classLoader;
	
	private final List<String> classNames;
	private final BiMap<String,String> classMappings = HashBiMap.create();
	
	private final StringTreeMap srgFieldMaps = new StringTreeMap();
	private final StringTreeMap srgMethodMaps = new StringTreeMap();
	
	private final StringTreeMap mergedFieldMaps;
	private final StringTreeMap mergedMethodMaps;
	
	private final List<String> blankFieldCache = new ArrayList<>();
	private final List<String> blankMethodCache = new ArrayList<>();
	
	private final StringTreeMap fieldTypes = new StringTreeMap();
	
	@SneakyThrows
	public DeobfuscationRemapper() {
		classLoader = (LaunchClassLoader) getClass().getClassLoader();
		
		InputStream in = DeobfuscationData.getMappingData();
		
		if (in == null)
			throw new RuntimeException("Failed to retrieve deobfuscation mappings.");
		
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		
		InputStreamReader rin = new InputStreamReader(in);
		try (BufferedReader bin = new BufferedReader(rin)) {
			Splitter splitter = Splitter.on(CharMatcher.anyOf(": ")).omitEmptyStrings().trimResults();
			
			while (bin.ready()) {
				String line = bin.readLine();
				String[] parts = Iterables.toArray(splitter.split(line), String.class);
				
				switch (parts[0]) {
				case "PK":
					break;
				case "CL":
					builder.add(parts[2]);
					break;
				case "FD":
					readFieldMapping(parts);
					break;
				case "MD":
					readMethodMapping(parts);
					break;
				}
			}
		}
		
		classNames = builder.build();
		mergedFieldMaps = new StringTreeMap(srgFieldMaps.size());
		mergedMethodMaps = new StringTreeMap(srgMethodMaps.size());
	}
	
	private void readFieldMapping(String[] parts) throws Exception {
		String oldSrg = parts[1];
		int lastOld = oldSrg.lastIndexOf('/');
		String oldClass = oldSrg.substring(0, lastOld);
		String oldName = oldSrg.substring(lastOld+1);
		
		String newSrg = parts[2];
		int lastNew = newSrg.lastIndexOf('/');
		String newClass = newSrg.substring(0, lastNew);
		String newName = newSrg.substring(lastNew+1);
		
		String fieldType = findFieldType(oldClass, newClass, oldName);
		
		srgFieldMaps.get(newClass).put(oldName + ":" + fieldType, newName);
		srgFieldMaps.get(newClass).put(oldName + ":null", newName);
	}
	
	private synchronized String findFieldType(String oldOwner, String owner, String name) throws Exception {
		if (fieldTypes.containsKey(owner))
			return fieldTypes.get(owner).get(name);
		
		if (oldOwner == null)
			return null;
		
		byte[] classData = classLoader.getClassBytes(oldOwner);
		ClassNode clazz = new ClassNode();
		new ClassReader(classData).accept(clazz, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		
		Map<String,String> map = new HashMap<>();
		for (FieldNode field : clazz.fields)
			map.put(field.name, field.desc);
		
		fieldTypes.put(owner, map);
		return map.get(name);
	}
	
	private void readMethodMapping(String[] parts) {
		String oldSrg = parts[1];
		int lastOld = oldSrg.lastIndexOf('/');
		String oldName = oldSrg.substring(lastOld+1);
		
		String signature = parts[2];
		
		String newSrg = parts[3];
		int lastNew = newSrg.lastIndexOf('/');
		String newClass = newSrg.substring(0, lastNew);
		String newName = newSrg.substring(lastNew+1);
		
		srgMethodMaps.get(newClass).put(oldName + signature, newName);
	}
	
	@Override
	public synchronized String map(String type) {
		return classMappings.computeIfAbsent(type, unused -> findClassMapping(type));
	}
	
	private String findClassMapping(String type) {
		if ((!type.startsWith("org/bukkit/craftbukkit/") && !type.startsWith("net/minecraft/server/")) || type.equals("org/bukkit/craftbukkit/Main"))
			return type;
		
		if (type.startsWith("net/minecraft/server/")) {
			int lastType = type.lastIndexOf('/');
			String name = type.substring(lastType+1);
			
			for (String className : classNames) {
				int lastCheck = className.lastIndexOf('/');
				String checkName = className.substring(lastCheck+1);
				
				if (name.equals(checkName))
					return checkName;
			}
		}
		
		if (type.startsWith("org/bukkit/craftbukkit/")) {
			String[] parts = type.split("/");
			
			List<String> partList = new ArrayList<>();
			for (int i = 0; i < parts.length; i++)
				if (i != 3)
					partList.add(parts[i]);
			
			return String.join("/", partList);
		}
		
		return type;
	}
	
	public String unmap(String type) {
		return classMappings.inverse().get(type);
	}
	
	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		return mapFieldName(owner, name, descriptor, false);
	}
	
	public String mapMemberFieldName(String owner, String name, String descriptor) {
		String mappedName = mapFieldName(owner, name, descriptor, true);
		storeMemberFieldMapping(owner, name, descriptor, mappedName);
		return mappedName;
	}
	
	private void storeMemberFieldMapping(String owner, String name, String descriptor, String mappedName) {
		Map<String,String> fieldMap = srgFieldMaps.get(map(owner));
		
		String key = name + ":" + descriptor;
		String altKey = name + ":null";
		
		if (!fieldMap.containsKey(key)) {
			fieldMap.put(key, mappedName);
			fieldMap.put(altKey, mappedName);
			
			mergedFieldMaps.remove(owner);
		}
	}
	
	private String mapFieldName(String owner, String name, String descriptor, boolean member) {
		Map<String,String> fieldMap = getFieldMap(map(owner), member);
		return fieldMap != null && fieldMap.containsKey(name + ":" + descriptor) ? fieldMap.get(name + ":" + descriptor) : fieldMap != null && fieldMap.containsKey(name + ":null") ? fieldMap.get(name + ":null") : name;
	}
	
	private Map<String,String> getFieldMap(String clazz, boolean member) {
		if (member)
			return srgFieldMaps.get(clazz);
		
		if (!mergedFieldMaps.containsKey(clazz) && !blankFieldCache.contains(clazz)) {
			mergeParentMaps(clazz);
			
			if (!mergedFieldMaps.containsKey(clazz))
				blankFieldCache.add(clazz);
		}
		
		return mergedFieldMaps.get(clazz);
	}
	
	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		Map<String,String> methodMap = getMethodMap(map(owner));
		return methodMap != null && methodMap.containsKey(name + descriptor) ? methodMap.get(name + descriptor) : name;
	}
	
	@Override
	public String mapSignature(String signature, boolean typeSignature) {
		if (signature != null && signature.contains("!*"))
			return null;
		
		return super.mapSignature(signature, typeSignature);
	}
	
	private Map<String,String> getMethodMap(String clazz) {
		if (!mergedMethodMaps.containsKey(clazz) && !blankMethodCache.contains(clazz)) {
			mergeParentMaps(clazz);
			
			if (!mergedMethodMaps.containsKey(clazz))
				blankMethodCache.add(clazz);
		}
		
		return mergedMethodMaps.get(clazz);
	}
	
	@SneakyThrows
	private void mergeParentMaps(String name) {
		String superName = null;
		String[] interfaces = new String[0];
		
		byte[] classData = classLoader.getClassBytes(classMappings.inverse().get(name));
		if (classData != null) {
			ClassReader cr = new ClassReader(classData);
			superName = cr.getSuperName();
			interfaces = cr.getInterfaces();
		}
		
		mergeParentMaps(name, superName, interfaces);
	}
	
	public void mergeParentMaps(String name, String superName, String[] interfaces) {
		if (Strings.isNullOrEmpty(superName))
			return;
		
		List<String> parents = ImmutableList.<String>builder()
				.add(superName)
				.addAll(Arrays.asList(interfaces))
		.build();
		
		for (String parent : parents)
			if (!mergedFieldMaps.containsKey(parent))
				mergeParentMaps(parent);
		
		Map<String,String> fieldMap = new HashMap<>();
		Map<String,String> methodMap = new HashMap<>();
		
		for (String parent : parents) {
			if (mergedFieldMaps.containsKey(parent))
				fieldMap.putAll(mergedFieldMaps.get(parent));
			
			if (mergedMethodMaps.containsKey(parent))
				methodMap.putAll(mergedMethodMaps.get(parent));
		}
		
		if (srgFieldMaps.containsKey(name))
			fieldMap.putAll(srgFieldMaps.get(name));
		
		if (srgMethodMaps.containsKey(name))
			methodMap.putAll(srgMethodMaps.get(name));
		
		mergedFieldMaps.put(name, ImmutableMap.copyOf(fieldMap));
		mergedMethodMaps.put(name, ImmutableMap.copyOf(methodMap));
	}
	
	@SneakyThrows
	public String getStaticFieldType(String oldType, String oldName, String newType, String newName) {
		String replType = findFieldType(null, newType, newName);
		if (oldType.equals(newType))
			return replType;
		
		Map<String,String> newClassMap = fieldTypes.get(newType);
		newClassMap.put(newName, replType);
		return replType;
	}
}
