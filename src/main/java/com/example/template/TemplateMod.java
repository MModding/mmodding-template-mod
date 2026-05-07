package com.example.template;

import com.mmodding.library.core.api.AdvancedContainer;
import com.mmodding.library.core.api.ExtendedModInitializer;
import com.mmodding.library.core.api.management.ElementsManager;
import com.mmodding.library.core.api.registry.IdentifierUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class TemplateMod implements ExtendedModInitializer {

	@Override
	public void setupManager(ElementsManager elementsManager) {
	}

	@Override
	public void onInitialize(AdvancedContainer mod) {
		mod.logger().info("Hello MModding World!");
	}

	public static String namespace() {
		return "template";
	}

	public static Identifier createId(String path) {
		return Identifier.fromNamespaceAndPath(namespace(), path);
	}

	public static Identifier createTexture(String path) {
		return IdentifierUtil.texture(namespace(), path);
	}

	public static <T> ResourceKey<T> createKey(ResourceKey<? extends Registry<T>> registry, String path) {
		return ResourceKey.create(registry, createId(path));
	}
}
