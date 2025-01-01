package com.github.synnerz.talium.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.awt.Color

object Renderer {
    private val tessellator: Tessellator by lazy { Tessellator.getInstance() }
    private val worldRenderer: WorldRenderer by lazy { tessellator.worldRenderer }

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
            this.red.toFloat(),
            this.green.toFloat(),
            this.blue.toFloat(),
            this.alpha.toFloat()
        )
    }

    fun Color.unbind() {
        GlStateManager.color(0f, 0f, 0f, 0f)
    }
}