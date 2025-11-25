package com.github.synnerz.talium.utils.state

import com.github.synnerz.talium.utils.Renderer
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.TextureSetup
import org.joml.Matrix3x2f
import java.awt.Color

class ColorCustomQuadState(
    val stack: Matrix3x2f,
    val x1: Int, val y1: Int,
    val x2: Int, val y2: Int,
    val color: Color = Color.WHITE
) : SimpleGuiElementRenderState {
    private val BLACK = Color.BLACK.rgb
    private val WHITE = Color.WHITE.rgb

    override fun bounds(): ScreenRect? {
        return ScreenRect(x1, y1, x2 - x1, y2 - y1).transformEachVertex(stack)
    }

    override fun setupVertices(vertices: VertexConsumer?, depth: Float) {
        vertices?.vertex(x1.toFloat(), y2.toFloat(), depth)?.color(BLACK)
        vertices?.vertex(x2.toFloat(), y2.toFloat(), depth)?.color(BLACK)
        vertices?.vertex(x2.toFloat(), y1.toFloat(), depth)?.color(color.rgb)
        vertices?.vertex(x1.toFloat(), y1.toFloat(), depth)?.color(WHITE)
    }

    override fun pipeline(): RenderPipeline {
        return RenderPipelines.GUI
    }

    override fun textureSetup(): TextureSetup {
        return TextureSetup.empty()
    }

    override fun scissorArea(): ScreenRect? {
        return null
    }
}