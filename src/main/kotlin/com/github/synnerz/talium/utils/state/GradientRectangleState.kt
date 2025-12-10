package com.github.synnerz.talium.utils.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.TextureSetup
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
    val scissorArea: ScreenRect? = null,
    bounds: ScreenRect? = null,
) : SimpleGuiElementRenderState {
    constructor(
        pose: Matrix3x2f,
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        col11: Int, col21: Int,
        col12: Int, col22: Int,
        pipeline: RenderPipeline? = null,
        textureSetup: TextureSetup? = null,
        scissorArea: ScreenRect? = null,
        bounds: ScreenRect? = null,
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
    override fun textureSetup(): TextureSetup = textureSetup ?: TextureSetup.empty()
    override fun scissorArea(): ScreenRect? = scissorArea
    override fun bounds(): ScreenRect? = bounds

    override fun setupVertices(vertices: VertexConsumer) {
        vertices.vertex(pose, x1, y2).color(col12)
        vertices.vertex(pose, x2, y2).color(col22)
        vertices.vertex(pose, x2, y1).color(col21)
        vertices.vertex(pose, x1, y1).color(col11)
    }

    private fun createBounds(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        pose: Matrix3x2f,
        scissorArea: ScreenRect?,
    ): ScreenRect? {
        val screenRect = ScreenRect(
            x1, y1,
            x2 - x1, y2 - y1
        ).transformEachVertex(pose)
        return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
    }
}