package com.github.synnerz.talium.mixin;

import com.github.synnerz.talium.utils.MouseState;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Inject(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void talium$onPreScroll(long window, double horizontal, double vertical, CallbackInfo ci, boolean bl, double d, double hrz, double vrt, double mx, double my) {
        MouseState.INSTANCE.triggerMouseScroll(mx, my, (int) vrt);
    }

    @Inject(
            method = "onButton",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;gui:Lnet/minecraft/client/gui/Gui;",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 3
            )
    )
    private void talium$onClick(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        MouseState.INSTANCE.getButtonsDown().put(rawButtonInfo.button(), action);
    }
}
