package com.simibubi.create.gametest.mixin;

import com.simibubi.create.foundation.mixin.accessor.GameTestHelperAccessor;

import io.github.fabricators_of_create.porting_lib.gametest.infrastructure.CustomGameTestHelper;
import io.github.fabricators_of_create.porting_lib.gametest.infrastructure.ExtendedTestFunction;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

// Porting Lib beta.90 validates custom helpers but invokes its test consumer with a vanilla helper.
@Mixin(value = ExtendedTestFunction.class, remap = false)
public abstract class PortingLibGameTestHelperMixin {
    @Inject(method = "asConsumer", at = @At("RETURN"), cancellable = true, remap = false)
    private static void create$adaptCustomHelper(
            Method method, CallbackInfoReturnable<Consumer<GameTestHelper>> callback) {
        CustomGameTestHelper annotation = method.getAnnotation(CustomGameTestHelper.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(CustomGameTestHelper.class);
        }
        if (annotation == null) {
            return;
        }
        Constructor<? extends GameTestHelper> constructor;
        try {
            constructor = annotation.value().getConstructor(GameTestInfo.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Invalid custom GameTest helper", e);
        }
        Consumer<GameTestHelper> original = callback.getReturnValue();
        callback.setReturnValue(
                helper -> {
                    GameTestHelperAccessor source = (GameTestHelperAccessor) helper;
                    GameTestHelper custom;
                    try {
                        custom = constructor.newInstance(source.getTestInfo());
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Cannot create custom GameTest helper", e);
                    }
                    GameTestHelperAccessor target = (GameTestHelperAccessor) custom;
                    target.setFinalCheckAdded(source.getFinalCheckAdded());
                    try {
                        original.accept(custom);
                    } finally {
                        source.setFinalCheckAdded(target.getFinalCheckAdded());
                    }
                });
    }
}
