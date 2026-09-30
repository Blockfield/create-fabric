package com.simibubi.create.impl.registry;

import com.simibubi.create.api.registry.SimpleRegistry;

import io.github.fabricators_of_create.porting_lib.resources.events.TagsUpdatedEvent;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TagProviderImpl<K, V> implements SimpleRegistry.Provider<K, V> {
    private final TagKey<K> tag;
    private final Function<K, Holder<K>> holderGetter;
    private final V value;

    public TagProviderImpl(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
        this.tag = tag;
        this.holderGetter = holderGetter;
        this.value = value;
    }

    @Override
    @Nullable
    public V get(K object) {
        Holder<K> holder = this.holderGetter.apply(object);
        return holder.is(this.tag) ? this.value : null;
    }

    @Override
    public void onRegister(Runnable invalidate) {
        TagsUpdatedEvent.EVENT.register(event -> invalidate.run());
    }

    // eye of the beholder? check the nametag, buddy
    public static Holder<BlockEntityType<?>> getBeHolder(BlockEntityType<?> type) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type);
    }
}
