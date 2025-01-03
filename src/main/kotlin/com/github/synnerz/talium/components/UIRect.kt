package com.github.synnerz.talium.components

import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.Renderer

open class UIRect @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override fun render() {
        if (radius == 0.0) {
            Renderer.drawRect(x, y, width, height)
            return
        }

        RoundedRect.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())
    }
}
