package com.github.synnerz.talium.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.StringUtils
import org.lwjgl.input.Mouse
import java.awt.Color

object Renderer {
    private val tessellator: Tessellator by lazy { Tessellator.getInstance() }
    private val worldRenderer: WorldRenderer by lazy { tessellator.worldRenderer }
    private val fontRenderer: FontRenderer by lazy { Minecraft.getMinecraft().fontRendererObj }
    private const val WHITE: Int = 0xFFFFFFFF.toInt()

    @JvmOverloads
    fun drawRect(x: Double, y: Double, width: Double, height: Double, solid: Boolean = true) = apply {
        worldRenderer.begin(if (solid) 6 else 2, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x, y + height, 0.0).endVertex()
        worldRenderer.pos(x + width, y + height, 0.0).endVertex()
        worldRenderer.pos(x + width, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
        tessellator.draw()
    }

    fun Color.bind() {
        GlStateManager.color(
            this.red.toFloat() / 255f,
            this.green.toFloat() / 255f,
            this.blue.toFloat() / 255f,
            this.alpha.toFloat() / 255f
        )
    }

    fun Color.unbind() {
        GlStateManager.color(0f, 0f, 0f, 0f)
    }

    fun getMouseX(sr: ScaledResolution): Float {
        val mx = Mouse.getX().toFloat()
        val rw = sr.scaledWidth.toFloat()
        val dw = Minecraft.getMinecraft().displayWidth.toFloat()
        return mx * rw / dw
    }

    fun getMouseY(sr: ScaledResolution): Float {
        val my = Mouse.getY().toFloat()
        val rh = sr.scaledHeight.toFloat()
        val dh = Minecraft.getMinecraft().displayHeight.toFloat()
        return rh - my * rh / dh - 1f
    }

    @JvmOverloads
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false, color: Int = WHITE) = apply {
        var _y = y
        GlStateManager.enableTexture2D()
        text.split('\n').forEach {
            fontRenderer.drawString(it, x, _y, color, shadow)
            _y += fontRenderer.FONT_HEIGHT
        }
        GlStateManager.disableTexture2D()
    }

    fun String.getWidth() = fontRenderer.getStringWidth(StringUtils.stripControlCodes(this))
}
