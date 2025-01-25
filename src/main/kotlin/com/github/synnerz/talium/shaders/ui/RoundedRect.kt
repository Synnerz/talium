package com.github.synnerz.talium.shaders.ui

import com.github.synnerz.talium.shaders.Shader
import com.github.synnerz.talium.shaders.uniform.*
import com.github.synnerz.talium.utils.Renderer

/**
 * Taken from Elementa under MIT License
 * [Link](https://github.com/EssentialGG/Elementa/blob/c8cb78334a073ca4554cb74d49a771e5320351c5/src/main/kotlin/club/sk1er/elementa/components/UIRoundedRectangle.kt)
 */
object RoundedRect {
    private var shader: Shader = Shader.fromResources("ui/rect/rect", "ui/rect/rounded_rect")
    private var shaderRadiusUniform: FloatUniform = FloatUniform(shader.getUniformLoc("u_Radius"))
    private var shaderInnerRectUniform: Vec4Uniform = Vec4Uniform(shader.getUniformLoc("u_InnerRect"))

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        if (!shader.usable) return

        shader.bind()

        shaderRadiusUniform.setValue(radius)
        shaderInnerRectUniform.setValue(
            Vector4f(
            x + radius,
            y + radius,
            (x + width) - radius,
            (y + height) - radius)
        )

        Renderer.drawRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        shader.unbind()
    }
}
