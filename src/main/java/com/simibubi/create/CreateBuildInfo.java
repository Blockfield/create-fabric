package com.simibubi.create;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.Util;

public class CreateBuildInfo {
    public static final String VERSION =
            Util.make(
                    () -> {
                        ModContainer container =
                                FabricLoader.getInstance().getModContainer(Create.ID).orElseThrow();
                        return container
                                .getMetadata()
                                .getVersion()
                                .getFriendlyString()
                                .split("\\+")[0];
                    });
}
