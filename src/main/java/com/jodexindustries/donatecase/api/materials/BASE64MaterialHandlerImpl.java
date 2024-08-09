package com.jodexindustries.donatecase.api.materials;

import com.jodexindustries.donatecase.api.data.material.MaterialHandler;
import day.dean.skullcreator.SkullCreator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BASE64MaterialHandlerImpl implements MaterialHandler {

    @Override
    public @NotNull ItemStack handle(@NotNull String context) {
        return SkullCreator.itemFromBase64(context);
    }
}
