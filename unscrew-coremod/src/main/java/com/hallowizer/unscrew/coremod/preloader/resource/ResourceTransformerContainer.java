package com.hallowizer.unscrew.coremod.preloader.resource;

import com.hallowizer.unscrew.api.resource.IResourceTransformer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResourceTransformerContainer {
	@Getter
	private final String plugin;
	@Getter
	private final IResourceTransformer transformer;
}
