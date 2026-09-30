package com.simibubi.create.content.processing.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/** Проверка обеих форм результата: {@code ./gradlew codecCheck}. */
public class ProcessingOutputCodecCheck {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        ProcessingOutput bare = parse("{\"count\":8,\"id\":\"minecraft:stone\"}");
        if (bare.getStack().getCount() != 8)
            throw new AssertionError("bare count " + bare.getStack().getCount());

        ProcessingOutput field = parse("{\"item\":{\"id\":\"minecraft:stone\"},\"count\":8}");
        if (field.getStack().getCount() != 8)
            throw new AssertionError("field count " + field.getStack().getCount());

        String encoded =
                ProcessingOutput.CODEC.encodeStart(JsonOps.INSTANCE, bare).getOrThrow().toString();
        if (!encoded.startsWith("{\"item\":{"))
            throw new AssertionError("encoded not in item form: " + encoded);

        System.out.println("OK encoded=" + encoded);
    }

    private static ProcessingOutput parse(String json) {
        return ProcessingOutput.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();
    }
}
