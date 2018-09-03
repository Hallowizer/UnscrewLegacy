package com.hallowizer.unscrew.coremod.preloader.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.hallowizer.unscrew.api.TransformerException;
import com.hallowizer.unscrew.api.resource.IResourceTransformer;
import com.hallowizer.unscrew.api.resource.Resource;
import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;
import com.hallowizer.unscrew.coremod.preloader.resource.url.TransformingStreamHandler;
import com.hallowizer.unscrew.coremod.transformer.BootstrapTransformer;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceManager {
	private final List<ResourceTransformerContainer> transformers = new ArrayList<>();
	private final Map<ResourceDescription,byte[]> transformedResourceCache = new HashMap<>();
	private final DefaultResourceContext ctx = new DefaultResourceContext();
	private final Map<URL,String> sourceCache = new HashMap<>();
	
	public void registerTransformer(String plugin, IResourceTransformer transformer) {
		transformers.add(new ResourceTransformerContainer(plugin, transformer));
	}
	
	// Transforming Methods
	
	public byte[] transformClassAsResource(String name, byte[] data) {
		return getTransformedResource("/" + name.replace('.', '/') + ".class", getSource(BootstrapTransformer.currentURL.get()), data);
	}
	
	private String getSource(URL url) {
		return sourceCache.computeIfAbsent(url, unused -> findSource(url));
	}
	
	@SneakyThrows
	private String findSource(URL url) {
		File file = new File(url.toURI());
		try (JarFile jar = new JarFile(file)) {
			JarEntry pluginYaml = jar.getJarEntry("plugin.yml");
			if (pluginYaml == null)
				return jar.getJarEntry("org/bukkit/craftbukkit/Main") == null ? "Unknown" : "Spigot";
			
			InputStream in = jar.getInputStream(pluginYaml);
			InputStreamReader rin = new InputStreamReader(in);
			
			Map<String,String> map = CorePluginLoader.getYaml().load(rin);
			return map.get("name") == null ? "Unknown" : map.get("name");
		}
	}
	
	public URL getResource(Class<?> clazz, String name) throws Exception {
		return getResource(clazz.getClassLoader(), name);
	}
	
	public InputStream getResourceAsStream(Class<?> clazz, String name) throws Exception {
		return getResourceAsStream(clazz.getClassLoader(), name);
	}
	
	public URL getResource(ClassLoader classLoader, String name) throws Exception {
		URL url = classLoader.getResource(name);
		
		Field handler = URL.class.getDeclaredField("handler");
		handler.setAccessible(true);
		handler.set(url, new TransformingStreamHandler(url, (URLStreamHandler) handler.get(url), toByteArray(getResourceAsStream(classLoader, name))));
		
		return url;
	}
	
	private byte[] toByteArray(InputStream in) throws Exception {
		try (BufferedInputStream bin = new BufferedInputStream(in)) {
			byte[] data = new byte[bin.available()];
			bin.read(data);
			return data;
		}
	}
	
	public InputStream getResourceAsStream(ClassLoader classLoader, String name) throws Exception {
		InputStream in = classLoader.getResourceAsStream(name);
		return getTransformedResource(name, findSource(classLoader.getResource(name)), in);
	}
	
	public InputStream getTransformedPluginYaml(JarFile jar, JarEntry entry) throws Exception {
		InputStream in = jar.getInputStream(entry);
		byte[] data = toByteArray(in);
		String str = new String(data);
		
		Map<String,String> map = CorePluginLoader.getYaml().load(str);
		byte[] newData = getTransformedResource("plugin.yml", map.get("name") == null ? "Unknown" : map.get("name"), data);
		return new ByteArrayInputStream(newData);
	}
	
	// Transformer Invocation
	
	private InputStream getTransformedResource(String name, String source, InputStream in) throws Exception {
		return new ByteArrayInputStream(getTransformedResource(name, source, toByteArray(in)));
	}
	
	private byte[] getTransformedResource(String name, String source, byte[] oldData) {
		return transformedResourceCache.computeIfAbsent(new ResourceDescription(name, source), unused -> runTransformers(name, source, oldData));
	}
	
	private byte[] runTransformers(String name, String source, byte[] data) {
		for (ResourceTransformerContainer transformer : transformers)
			data = runTransformer(transformer, name, source, data);
		
		return data;
	}
	
	private byte[] runTransformer(ResourceTransformerContainer transformer, String name, String source, byte[] oldData) {
		Resource oldResource = new DefaultResource(name, source, oldData);
		Resource newResource;
		
		ctx.setSource(transformer.getPlugin());
		try {
			newResource = transformer.getTransformer().transform(ctx, oldResource);
		} catch (TransformerException e) {
			throw e;
		} catch (Exception e) {
			throw new TransformerException(transformer.getPlugin() + "'s transformer " + transformer.getTransformer().getClass().getName() + " failed to transform the resource " + name + ". Please contact the author if this problem persists.", e);
		}
		
		return newResource.asByteArray();
	}
}
