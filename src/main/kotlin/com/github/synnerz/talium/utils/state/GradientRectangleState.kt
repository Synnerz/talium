package com.github.synnerz.talium.utils.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f
import kotlin.math.ceil

class GradientRectangleState(
    val pose: Matrix3x2f,
    val x1: Float, val y1: Float,
    val x2: Float, val y2: Float,
    val col11: Int, val col21: Int,
    val col12: Int, val col22: Int,
    val pipeline: RenderPipeline? = null,
    val textureSetup: TextureSetup? = null,
    val scissorArea: ScreenRectangle? = null,
    bounds: ScreenRectangle? = null,
) : GuiElementRenderState {
    constructor(
        pose: Matrix3x2f,
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        col11: Int, col21: Int,
        col12: Int, col22: Int,
        pipeline: RenderPipeline? = null,
        textureSetup: TextureSetup? = null,
        scissorArea: ScreenRectangle? = null,
        bounds: ScreenRectangle? = null,
    ) : this(
        pose,
        x1.toFloat(), y1.toFloat(),
        x2.toFloat(), y2.toFloat(),
        col11, col21,
        col12, col22,
        pipeline,
        textureSetup,
        scissorArea,
        bounds,
    )

    val bounds = bounds ?: createBounds(
        x1.toInt(), y1.toInt(),
        ceil(x2).toInt(), ceil(y2).toInt(),
        pose,
        scissorArea
    )

    override fun pipeline(): RenderPipeline = pipeline ?: RenderPipelines.GUI
    override fun textureSetup(): TextureSetup = textureSetup ?: TextureSetup.noTexture()
    override fun scissorArea(): ScreenRectangle? = scissorArea
    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x1, y2).setColor(col12)
        vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(col22)
        vertexConsumer.addVertexWith2DPose(pose, x2, y1).setColor(col21)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(col11)
    }

    private fun createBounds(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        pose: Matrix3x2f,
        scissorArea: ScreenRectangle?,
    ): ScreenRectangle? {
        val screenRect = ScreenRectangle(
            x1, y1,
            x2 - x1, y2 - y1
        ).transformAxisAligned(pose)
        return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
    }
}