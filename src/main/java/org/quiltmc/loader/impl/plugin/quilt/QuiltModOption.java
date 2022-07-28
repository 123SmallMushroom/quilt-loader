package org.quiltmc.loader.impl.plugin.quilt;

import java.nio.file.Path;

import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.quiltmc.loader.api.plugin.gui.PluginGuiIcon;
import org.quiltmc.loader.impl.metadata.qmj.InternalModMetadata;
import org.quiltmc.loader.impl.plugin.base.InternalModOptionBase;
import org.quiltmc.loader.impl.plugin.gui.GuiManagerImpl;

public class QuiltModOption extends InternalModOptionBase {

	public QuiltModOption(QuiltPluginContext pluginContext, InternalModMetadata meta, Path from, Path resourceRoot,
		boolean mandatory) {

		super(pluginContext, meta, from, resourceRoot, mandatory);
	}

	@Override
	public PluginGuiIcon modTypeIcon() {
		return GuiManagerImpl.ICON_QUILT;
	}

	@Override
	public ModContainerExt convertToMod(Path transformedResourceRoot) {
		return new QuiltModContainer(pluginContext.pluginId(), metadata, from, transformedResourceRoot);
	}
}