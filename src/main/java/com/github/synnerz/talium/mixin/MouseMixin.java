package com.github.synnerz.talium.mixin;

import com.github.synnerz.talium.utils.MouseState;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(
            method = "onMouseScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void talium$onPreScroll(long window, double horizontal, double vertical, CallbackInfo ci, boolean bl, double d, double hrz, double vrt, double mx, double my) {
        MouseState.INSTANCE.triggerMouseScroll(mx, my, (int) vrt);
    }

    @Inject(
            method = "onMouseButton",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void talium$onClick(long window, MouseInput input, int action, CallbackInfo ci) {
        MouseState.INSTANCE.getButtonsDown().put(input.button(), action);
    }
}
