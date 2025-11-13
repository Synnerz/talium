package com.github.synnerz.talium.mixin;

import com.github.synnerz.talium.utils.MouseState;
import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(
            method = "onMouseScroll",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;options:Lnet/minecraft/client/option/GameOptions;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void talium$onPreScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseState.INSTANCE.setDWheel((int) vertical);
    }

    @Inject(
            method = "onMouseScroll",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;options:Lnet/minecraft/client/option/GameOptions;",
                    opcode = Opcodes.GETFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void talium$onPostScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseState.INSTANCE.setDWheel(0);
    }

    @Inject(
            method = "onMouseButton",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void talium$onClick(long window, int button, int action, int mods, CallbackInfo ci) {
        MouseState.INSTANCE.getButtonsDown().put(button, action);
    }
}
