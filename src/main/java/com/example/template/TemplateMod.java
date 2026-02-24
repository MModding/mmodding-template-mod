package com.example.template;

import com.mmodding.library.core.api.AdvancedContainer;
import com.mmodding.library.core.api.ExtendedModInitializer;
import com.mmodding.library.core.api.management.ElementsManager;

public class TemplateMod implements ExtendedModInitializer {

	@Override
	public void setupManager(ElementsManager.Builder builder) {}

	@Override
	public void onInitialize(AdvancedContainer mod) {
		mod.logger().info("Hello MModding world!");
	}
}