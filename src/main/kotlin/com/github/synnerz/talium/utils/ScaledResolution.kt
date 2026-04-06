package com.github.synnerz.talium.utils

import net.minecraft.client.Minecraft

class ScaledResolution(val mc: Minecraft) {
    val scaledWidth = mc.window.guiScaledWidth
    val scaledHeight = mc.window.guiScaledHeight
    val scaleFactor = mc.window.guiScale
    val scaledWidth_double get() = scaledWidth.toDouble()
    val scaledHeight_double get() = scaledHeight.toDouble()
}