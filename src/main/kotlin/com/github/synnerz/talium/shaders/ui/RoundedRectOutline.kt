package com.github.synnerz.talium.shaders.ui

import com.github.synnerz.talium.shaders.Shader
import com.github.synnerz.talium.shaders.uniform.*
import com.github.synnerz.talium.utils.Renderer

object RoundedRectOutline {
    private val shader = Shader.fromResources("ui/rect/rect", "ui/rect/rounded_rect_outline")
    private val shaderRadiusUniform: FloatUniform = FloatUniform(shader.getUniformLoc("u_Radius"))
    private val shaderInnerRectUniform: Vec4Uniform = Vec4Uniform(shader.getUniformLoc("u_InnerRect"))
    private val shaderOutlineWidth: FloatUniform = FloatUniform(shader.getUniformLoc("u_OutlineWidth"))

    fun drawRoundedRectOutline(x: Float, y: Float, width: Float, height: Float, radius: Float, lineWidth: Float = 0.5f) {
        if (!shader.usable) return

        shader.bind()

        shaderRadiusUniform.setValue(radius)
        shaderInnerRectUniform.setValue(
            Vector4f(
                x,
                y,
                x + width,
                y + height)
        )
        shaderOutlineWidth.setValue(lineWidth)

        Renderer.drawRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        shader.unbind()
    }
}
