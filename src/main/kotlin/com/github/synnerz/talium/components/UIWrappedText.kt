package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer.drawString
import com.github.synnerz.talium.utils.Renderer.fontRenderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import com.github.synnerz.talium.utils.Renderer.stack
import java.awt.Color

open class UIWrappedText @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var text: String = "",
    var centered: Boolean = false,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var bgColor: Color = Color.WHITE
    open var textScale: Float = 1f

    override fun render() {
        if (centered) return drawWrappedStringCentered(text, x, y, width, height, textScale, bgColor)

        drawWrappedString(text, x, y, width, height, textScale, bgColor)
    }

    companion object {
        @JvmOverloads
        fun drawWrappedString(
            str: String,
            x: Double, y: Double,
            width: Double, height: Double,
            scale: Float = 1f,
            color: Color = Color.WHITE
        ) {
            var toRender = ""
            var addedNW = 0
            val limitHeight = height * scale
            var currentWidth = 0
            var lastChar = Char.MIN_VALUE
            var lastFormat = ""

            for (char in str) {
                if (addedNW * (9 * scale) >= limitHeight) break
                currentWidth += "$char".getWidth()
                toRender += char
                if (lastChar == '§' && char != '§') lastFormat = "§$char"
                lastChar = char

                if ((currentWidth * scale) < width) continue

                addedNW++
                toRender += "\n$lastFormat"
                currentWidth = 0
            }

            if (scale != 1f) {
                stack().pushMatrix()
                stack().scale(scale, scale)
            }

            drawString(toRender.ifEmpty { str }, x.toFloat() / scale, y.toFloat() / scale, color = color.rgb)

            if (scale != 1f) stack().popMatrix()
        }

        @JvmOverloads
        fun drawWrappedStringCentered(
            str: String,
            x: Double, y: Double,
            width: Double, height: Double,
            scale: Float = 1f,
            color: Color = Color.WHITE
        ) {
            var currentString = ""
            var currentWidth = 0
            val limitHeight = height * scale
            val fixedString = mutableListOf<String>()
            var lastChar = Char.MIN_VALUE
            var lastFormat = ""

            str.forEachIndexed { idx, char ->
                if (fixedString.size * (9 * scale) >= limitHeight) return@forEachIndexed
                currentString += char
                currentWidth += "$char".getWidth()
                if (lastChar == '§' && char != '§') lastFormat = "§$char"
                lastChar = char

                if (currentWidth * scale >= width) {
                    fixedString.add("$lastFormat${currentString.trim()}")
                    currentString = ""
                    currentWidth = 0
                } else if (idx == str.length - 1) {
                    fixedString.add("$lastFormat${currentString.trim()}")
                }
            }

            if (scale != 1f) {
                stack().pushMatrix()
                stack().scale(scale, scale)
            }

            val renderText = fixedString.ifEmpty { str.split("\n") }
            val lineHeight = fontRenderer.fontHeight * scale
            val totalHeight = renderText.size * lineHeight
            var yy = 0

            renderText.forEach {
                val dy = y + yy
                val strwidth = it.getWidth() * scale

                drawString(
                    it,
                    (x + (width - strwidth) / 2.0).toFloat() / scale,
                    (dy + (height - totalHeight) / 2.0).toFloat() / scale,
                    true,
                    color.rgb
                )

                yy += lineHeight.toInt()
            }

            if (scale != 1f) stack().popMatrix()
        }
    }
}