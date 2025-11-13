package com.github.synnerz.talium.utils

import net.minecraft.client.MinecraftClient

class ScaledResolution(val mc: MinecraftClient) {
    val scaledWidth = mc.window.scaledWidth
    val scaledHeight = mc.window.scaledHeight
    val scaleFactor = mc.window.scaleFactor.toInt()
    val scaledWidth_double get() = scaledWidth.toDouble()
    val scaledHeight_double get() = scaledHeight.toDouble()
}