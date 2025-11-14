package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import com.github.synnerz.talium.utils.Renderer.stack
import java.awt.Color

open class UIText @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var text: String = "",
    var centered: Boolean = false,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var bgColor: Color = Color.WHITE
    open var textScale: Float = 1f

    override fun render() {
        if (centered) drawCenteredText(text, x, y, width, height, textScale, bgColor)
        else drawText(text, x, y, textScale, bgColor)
    }

    companion object {
        @JvmOverloads
        fun drawText(text: String, x: Double, y: Double, scale: Float = 1f, color: Color = Color.WHITE) {
            if (scale != 1f) {
                stack().push()
                stack().scale(scale, scale, 0f)
            }

            Renderer.drawString(
                text,
                x.toFloat() / scale,
                y.toFloat() / scale,
                true,
                color.rgb
            )

            if (scale != 1f) stack().pop()
        }

        @JvmOverloads
        fun drawCenteredText(text: String, x: Double, y: Double, width: Double, height: Double, scale: Float = 1f, color: Color = Color.WHITE) {
            val textWidth = text.getWidth() * scale
            val textHeight = 9f * scale

            if (scale != 1f) {
                stack().push()
                stack().scale(scale, scale, 0f)
            }

            Renderer.drawString(
                text,
                (x + (width - textWidth) / 2.0).toFloat() / scale,
                (y + (height - textHeight) / 2.0).toFloat() / scale,
                true,
                color.rgb
            )

            if (scale != 1f) stack().pop()
        }
    }
}
