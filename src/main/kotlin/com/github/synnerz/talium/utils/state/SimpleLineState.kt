package com.github.synnerz.talium.utils.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.TextureSetup
import org.joml.Matrix3x2f
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SimpleLineState(
    val stack: Matrix3x2f,
    val x1: Float, val y1: Float,
    val x2: Float, val y2: Float,
    val thickness: Float = 1f,
    val color: Color = Color.WHITE
) : SimpleGuiElementRenderState {
    override fun bounds(): ScreenRect? {
        return ScreenRect(x1.toInt(), y1.toInt(), x2.toInt() - x1.toInt(), y2.toInt() - y1.toInt()).transformEachVertex(stack)
    }

    override fun setupVertices(vertices: VertexConsumer?) {
        val theta = -atan2(y2 - y1, x2 - x1)
        val i = sin(theta) * (thickness / 2)
        val j = cos(theta) * (thickness / 2)

        vertices?.vertex(stack, x1 + i, y1 + j)?.color(color.rgb)
        vertices?.vertex(stack, x2 + i, y2 + j)?.color(color.rgb)
        vertices?.vertex(stack, x2 - i, y2 - j)?.color(color.rgb)
        vertices?.vertex(stack, x1 - i, y1 - j)?.color(color.rgb)
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