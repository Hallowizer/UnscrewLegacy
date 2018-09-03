package com.hallowizer.unscrew.coremod.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.experimental.UtilityClass;
import net.minecraft.launchwrapper.Launch;

@UtilityClass
public class UnscrewLauncher {
	public void main(String[] args) {
		List<String> argList = new ArrayList<>();
		argList.addAll(Arrays.asList(args));
		argList.addAll(Arrays.asList(
				"--version", "UnscrewBootstrap",
				"--gameDir", ".",
				"--assetsDir", ".",
				"--tweakClass", "com.hallowizer.unscrew.coremod.tweaker.BootstrapTweaker"
		));
		
		Launch.main((String[]) argList.toArray(new String[argList.size()]));
	}
}
