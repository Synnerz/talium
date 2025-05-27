package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer.drawString
import com.github.synnerz.talium.utils.Renderer.fontRenderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import net.minecraft.client.renderer.GlStateManager

open class UIWrappedText @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var text: String = "",
    var centered: Boolean = false,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open var textScale: Float = 1f

    override fun render() {
        if (centered) return drawWrappedStringCentered(text, x, y, width, height, textScale)

        drawWrappedString(text, x, y, width, height, textScale)
    }

    companion object {
        @JvmOverloads
        fun drawWrappedString(str: String, x: Double, y: Double, width: Double, height: Double, scale: Float = 1f) {
            var toRender = ""
            var addedNW = 0
            val limitHeight = height * scale
            var currentWidth = 0

            for (char in str) {
                if (addedNW * (9 * scale) >= limitHeight) break
                currentWidth += "$char".getWidth()
                toRender += char

                if ((currentWidth * scale) < width) continue

                addedNW++
                toRender += "\n"
                currentWidth = 0
            }

            if (scale != 1f) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 0f)
            }

            drawString(toRender.ifEmpty { str }, x.toFloat() / scale, y.toFloat() / scale)

            if (scale != 1f) GlStateManager.popMatrix()
        }

        @JvmOverloads
        fun drawWrappedStringCentered(str: String, x: Double, y: Double, width: Double, height: Double, scale: Float = 1f) {
            var currentString = ""
            var currentWidth = 0
            val limitHeight = height * scale
            val fixedString = mutableListOf<String>()

            str.forEachIndexed { idx, char ->
                if (fixedString.size * (9 * scale) >= limitHeight) return@forEachIndexed
                currentString += char
                currentWidth += "$char".getWidth()

                if (currentWidth * scale >= width) {
                    fixedString.add(currentString.trim())
                    currentString = ""
                    currentWidth = 0
                } else if (idx == str.length - 1) {
                    fixedString.add(currentString.trim())
                }
            }

            if (scale != 1f) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 0f)
            }
            GlStateManager.enableTexture2D()

            val renderText = fixedString.ifEmpty { str.split("\n") }
            val lineHeight = fontRenderer.FONT_HEIGHT * scale
            val totalHeight = renderText.size * lineHeight
            var yy = 0

            renderText.forEach {
                val dy = y + yy
                val strwidth = it.getWidth() * scale
                fontRenderer.drawString(
                    it,
                    (x + (width - strwidth) / 2.0).toFloat() / scale,
                    (dy + (height - totalHeight) / 2.0).toFloat() / scale,
                    0xFFFFFFFF.toInt(),
                    true)
                yy += lineHeight.toInt()
            }

            GlStateManager.disableTexture2D()

            if (scale != 1f) GlStateManager.popMatrix()
        }
    }
}