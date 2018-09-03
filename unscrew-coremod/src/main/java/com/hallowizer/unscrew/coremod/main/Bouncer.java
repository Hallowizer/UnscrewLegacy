package com.hallowizer.unscrew.coremod.main;

import java.lang.reflect.Method;

import com.hallowizer.unscrew.coremod.preloader.CorePluginLoader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Bouncer {
	public void main(String[] args) throws Exception {
		String mainName = CorePluginLoader.getBounceClass();
		Class<?> mainClass = Class.forName(mainName);
		
		Method main = mainClass.getDeclaredMethod("main", String[].class);
		main.setAccessible(true);
		main.invoke(null, (Object) args);
	}
}
