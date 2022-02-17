/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.quiltmc.loader.impl.game.minecraft;

import org.quiltmc.loader.api.minecraft.ClientModInitializer;
import org.quiltmc.loader.api.minecraft.DedicatedServerModInitializer;
import org.quiltmc.loader.api.minecraft.ModInitializer;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;

import java.io.File;

public final class Hooks {
	public static final String INTERNAL_NAME = Hooks.class.getName().replace('.', '/');

	public static String appletMainClass;

	public static final String QUILT = "quilt";
	public static final String VANILLA = "vanilla";

	public static String insertBranding(final String brand) {
		if (brand == null || brand.isEmpty()) {
			Log.warn(LogCategory.GAME_PROVIDER, "Null or empty branding found!", new IllegalStateException());
			return QUILT;
		}

		return VANILLA.equals(brand) ? QUILT : brand + ',' + QUILT;
	}

	public static void startClient(File runDir, Object gameInstance) {
		if (runDir == null) {
			runDir = new File(".");
		}

		QuiltLoaderImpl.INSTANCE.prepareModInit(runDir.toPath(), gameInstance);
		EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
		EntrypointUtils.invoke("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
	}

	public static void startServer(File runDir, Object gameInstance) {
		if (runDir == null) {
			runDir = new File(".");
		}

		QuiltLoaderImpl.INSTANCE.prepareModInit(runDir.toPath(), gameInstance);
		EntrypointUtils.invoke("main", ModInitializer.class, ModInitializer::onInitialize);
		EntrypointUtils.invoke("server", DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
	}

	public static void setGameInstance(Object gameInstance) {
		QuiltLoaderImpl.INSTANCE.setGameInstance(gameInstance);
	}
}