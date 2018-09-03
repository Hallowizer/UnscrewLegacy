package com.hallowizer.unscrew.commons.deobfuscation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.launchwrapper.LaunchClassLoader;

@UtilityClass
public class DeobfuscationData {
	private File srgFile;
	
	public InputStream getMappingData() throws Exception {
		return new FileInputStream(srgFile);
	}
	
	@SneakyThrows
	public void injectDeobfuscationTransformer(LaunchClassLoader classLoader) {
		setVersion(findVersion(classLoader));
		classLoader.registerTransformer("com.hallowizer.unscrew.commons.transformer.DeobfuscationTransformer");
	}
	
	private String findVersion(LaunchClassLoader classLoader) throws Exception {
		byte[] data = classLoader.getClassBytes("org.bukkit.craftbukkit.Main");
		
		ClassNode clazz = new ClassNode();
		new ClassReader(data).accept(clazz, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		
		for (MethodNode method : clazz.methods)
			for (AbstractInsnNode insn : (Iterable<AbstractInsnNode>) () -> method.instructions.iterator())
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode cast = (MethodInsnNode) insn;
					
					if (cast.owner.startsWith("net/minecraft/server/") && cast.owner.endsWith("/MinecraftServer")) {
						String[] parts = cast.owner.split("/");
						String versionString = parts[3]; // For example, v1_12_R1
						
						String versionNumber = versionString.split("_")[1];
						return "1." + versionNumber;
					}
				}
		
		throw new IllegalStateException("Unable to find Spigot version.");
	}
	
	@SneakyThrows
	public void setVersion(String version) {
		File mcpFolder = new File("mcp");
		if (!mcpFolder.exists())
			mcpFolder.mkdir();
		
		File srgFile = new File(mcpFolder, version + ".srg");
		if (!srgFile.exists())
			downloadMCP(version, srgFile);
		
		DeobfuscationData.srgFile = srgFile;
	}
	
	private void downloadMCP(String versionName, File file) throws Exception {
		MinecraftVersion version = new MinecraftVersion(versionName, true);
		
		ByteArrayOutputStream out;
		
		{
			InputStream in = DeobfuscationData.class.getResourceAsStream("/downloads.mcp");
			out = new ByteArrayOutputStream();
			ByteStreams.copy(in, out);
		}
		
		ByteSource bytes = ByteSource.wrap(out.toByteArray());
		CharSource charSource = bytes.asCharSource(Charsets.UTF_8);
		
		final List<MinecraftVersion> versions = new ArrayList<>();
		final Map<MinecraftVersion,URL> mcpDownloads = new HashMap<>();
		
		charSource.readLines(new LineProcessor<String>() {
			@Override
			public boolean processLine(String line) throws IOException {
				String contents = Splitter.on('#').trimResults().omitEmptyStrings().split(line).iterator().next();
				String[] parts = contents.split("=");
				
				MinecraftVersion mcVersion = new MinecraftVersion(parts[0], false);
				versions.add(mcVersion);
				mcpDownloads.put(mcVersion, new URL(parts[1]));
				return true;
			}
			
			@Override
			public String getResult() {
				return "EAT MORE YUMMY PIES";
			}
		});
		
		versions.add(version);
		versions.sort(null);
		
		MinecraftVersion mcpVersion = versions.get(versions.indexOf(version)-1);
		URL download = mcpDownloads.get(mcpVersion);
		
		InputStream in = download.openStream();
		ZipInputStream zin = new ZipInputStream(in);
		
		while (in.available() > 0) {
			ZipEntry entry = zin.getNextEntry();
			if (!entry.getName().equals("conf/joined.srg"))
				zin.closeEntry();
			else {
				byte[] data = new byte[zin.available()];
				zin.read(data);
				
				file.createNewFile();
				try (FileOutputStream fout = new FileOutputStream(file)) {
					fout.write(data);
				}
				return;
			}
		}
		
		throw new IllegalStateException("Version " + version + " is currently not supported. Please install your own SRG file if you have one.");
	}
}
