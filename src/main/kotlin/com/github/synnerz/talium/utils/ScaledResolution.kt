package com.github.synnerz.talium.utils

import net.minecraft.client.Minecraft

class ScaledResolution(val mc: Minecraft) {
    val scaledWidth get() = mc.window.guiScaledWidth
    val scaledHeight get() = mc.window.guiScaledHeight
    val scaleFactor get() = mc.window.guiScale
    val scaledWidth_double get() = scaledWidth.toDouble()
    val scaledHeight_double get() = scaledHeight.toDouble()
    val screenWidth get() = mc.window.screenWidth
    val screenHeight get() = mc.window.screenHeight
    val mxr get() = mc.mouseHandler.xpos()
    val myr get() = mc.mouseHandler.ypos()
}