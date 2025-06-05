package com.github.synnerz.talium.components

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import java.awt.Color

open class UIItem @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var item: ItemStack,
    var scale: Float = 1f,
    var zlevel: Float = 200f,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var bgColor: Color = Color(255, 255, 255, 255)

    override fun render() {
        val itemRenderer = Minecraft.getMinecraft().renderItem

        if (scale != 1f) {
            GlStateManager.scale(scale, scale, 1f)
        }

        GlStateManager.enableTexture2D()
        RenderHelper.enableStandardItemLighting()
        RenderHelper.enableGUIStandardItemLighting()
        itemRenderer.zLevel = zlevel
        itemRenderer.renderItemIntoGUI(item, (x / scale).toInt(), (y / scale).toInt())
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableTexture2D()

        if (scale != 1f) {
            GlStateManager.scale(1f / scale, 1f / scale, 1f)
        }
    }
}