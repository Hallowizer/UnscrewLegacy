package com.hallowizer.unscrew.coremod.tweaker;

import net.minecraft.launchwrapper.ITweaker;

public abstract class CascadedTweaker implements ITweaker {
	public final String getLaunchTarget() {
		throw new UnsupportedOperationException("Illegal primary tweaker");
	}
}
