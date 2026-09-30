package com.simibubi.create.foundation;

import com.simibubi.create.AllBlockEntityTypes;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class CreateNBTProcessors {
    public static void register() {

        NBTProcessors.addProcessor(
                BlockEntityType.SIGN,
                data -> {
                    for (int i = 0; i < 4; ++i) {
                        if (hasClickEvent(data.getString("Text" + (i + 1)))) return null;
                    }
                    return data;
                });

        NBTProcessors.addProcessor(
                BlockEntityType.LECTERN,
                data -> {
                    if (!data.contains("Book", Tag.TAG_COMPOUND)) return data;
                    CompoundTag book = data.getCompound("Book");

                    // Writable books can't have click events, so they're safe to keep
                    ResourceLocation writableBookResource =
                            BuiltInRegistries.ITEM.getKey(Items.WRITABLE_BOOK);
                    if (writableBookResource != BuiltInRegistries.ITEM.getDefaultKey()
                            && book.getString("id").equals(writableBookResource.toString()))
                        return data;

                    if (!book.contains("tag", Tag.TAG_COMPOUND)) return data;
                    CompoundTag tag = book.getCompound("tag");

                    if (!tag.contains("pages", Tag.TAG_LIST)) return data;
                    ListTag pages = tag.getList("pages", Tag.TAG_STRING);

                    for (Tag inbt : pages) {
                        if (hasClickEvent(inbt.getAsString())) return null;
                    }
                    return data;
                });

        NBTProcessors.addProcessor(
                AllBlockEntityTypes.CLIPBOARD.get(), CreateNBTProcessors::clipboardProcessor);

        NBTProcessors.addProcessor(
                AllBlockEntityTypes.CREATIVE_CRATE.get(), NBTProcessors.itemProcessor("Filter"));
        NBTProcessors.addProcessor(
                AllBlockEntityTypes.PLACARD.get(), NBTProcessors.itemProcessor("Item"));
    }

    public static CompoundTag clipboardProcessor(CompoundTag data) {
        if (!data.contains("Item", Tag.TAG_COMPOUND)) return data;
        CompoundTag book = data.getCompound("Item");

        if (!book.contains("tag", Tag.TAG_COMPOUND)) return data;
        CompoundTag itemData = book.getCompound("tag");

        for (List<String> entries :
                NBTHelper.readCompoundList(
                        itemData.getList("Pages", Tag.TAG_COMPOUND),
                        pageTag ->
                                NBTHelper.readCompoundList(
                                        pageTag.getList("Entries", Tag.TAG_COMPOUND),
                                        tag -> tag.getString("Text")))) {
            for (String entry : entries) if (hasClickEvent(entry)) return null;
        }
        return data;
    }

    /**
     * Catnip 0.8 принимает Component; строки из NBT — JSON-компоненты. Нечитаемый JSON считаем
     * безопасным.
     */
    private static boolean hasClickEvent(String json) {
        if (json == null || json.isEmpty()) return false;
        try {
            Component component = Component.Serializer.fromJson(json, RegistryAccess.EMPTY);
            return component != null && NBTProcessors.textComponentHasClickEvent(component);
        } catch (Exception e) {
            return false;
        }
    }
}
