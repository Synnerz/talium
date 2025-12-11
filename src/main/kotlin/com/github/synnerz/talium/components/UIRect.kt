package com.github.synnerz.talium.components

import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.Renderer
import java.awt.Color

open class UIRect @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override fun render() {
        drawRect(x, y, width, height, radius, bgColor)
    }

    companion object {
        @JvmOverloads
        fun drawRect(x: Double, y: Double, width: Double, height: Double, radius: Double = 0.0, color: Color = Color.WHITE) {
            if (radius == 0.0) {
                Renderer.drawRect(x, y, width, height, color = color)
                return
            }

            RoundedRect.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())
        }
    }
}
