package com.github.synnerz.talium.utils.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
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
    val color: Color = Color.WHITE,
    val _scissorArea: ScreenRectangle? = null
) : GuiElementRenderState {
    override fun bounds(): ScreenRectangle? {
        return ScreenRectangle(x1.toInt(), y1.toInt(), x2.toInt() - x1.toInt(), y2.toInt() - y1.toInt()).transformAxisAligned(stack)
    }

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val theta = -atan2(y2 - y1, x2 - x1)
        val i = sin(theta) * (thickness / 2)
        val j = cos(theta) * (thickness / 2)

        vertexConsumer.addVertexWith2DPose(stack, x1 + i, y1 + j).setColor(color.rgb)
        vertexConsumer.addVertexWith2DPose(stack, x2 + i, y2 + j).setColor(color.rgb)
        vertexConsumer.addVertexWith2DPose(stack, x2 - i, y2 - j).setColor(color.rgb)
        vertexConsumer.addVertexWith2DPose(stack, x1 - i, y1 - j).setColor(color.rgb)
    }

    override fun pipeline(): RenderPipeline {
        return RenderPipelines.GUI
    }

    override fun textureSetup(): TextureSetup {
        return TextureSetup.noTexture()
    }

    override fun scissorArea(): ScreenRectangle? {
        return _scissorArea
    }
}