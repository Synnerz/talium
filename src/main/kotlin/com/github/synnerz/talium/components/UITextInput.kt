package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIFocusEvent
import com.github.synnerz.talium.events.UIKeyType
import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.MathLib
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import kotlin.math.max

/**
 * * Component that handles input-like behavior
 */
open class UITextInput @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    var text: String = "",
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var cursorAnimation: Animation = Animation(Animations.CIRC_IN_OUT, 750f)
    var cursorPos: Int = 0
    var textScale: Float = 1f
    var cursorAlpha: Double = 255.0
    var cursorColorText: Int = 14737632
    var shouldBlink: Boolean = false

    override fun preDraw() {
        cursorAnimation.preDraw()
    }

    override fun render() {
        // TODO: add wrapped text or scissors effect to not go off bound
        // TODO: fix text scale on blinking `_`/`|` being offset by too much
        if (radius == 0.0) {
            Renderer.drawRect(x, y, width, height)
            if (textScale != 1f) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(textScale, textScale, 0f)
            }

            val textHeight = 9f * textScale
            val heightCenter = (height - textHeight) / 2
            Renderer.drawString(
                text,
                (x.toFloat() + 2f) / textScale,
                (y + heightCenter).toFloat() / textScale
                )

            if (textScale != 1f) {
                GlStateManager.popMatrix()
            }

            if (focused) {
                if (cursorAnimation.shouldAnimate) {
                    val ease = cursorAnimation.getEase()
                    val newMin = if (cursorAlpha == 255.0) 255.0 else 0.0
                    val newMax = if (cursorAlpha == 255.0) 0.0 else 255.0
                    cursorAlpha = MathLib.rescale(ease, 0.0, 1.0, newMin, newMax)
                    shouldBlink = ease >= 1.4f
                } else cursorAnimation.start()

                val n = text.substring(0, cursorPos).getWidth() + 2.0 / textScale
                // Check whether the current [cursorPos] is equals to the max string length
                // this means that the cursor is at the end, so we can change the rendering to be `_` instead of `|`
                if (cursorPos == text.length) {
                    // Scuffed blinking but it works i guess
                    if (!shouldBlink)
                        Renderer.drawString("_", ((x + n) / textScale).toFloat(), ((y + 2.0) / textScale).toFloat(), color = cursorColorText)
                }
                // Else we render the blinking `|`
                else {
                    GlStateManager.color(0f, 0f, 255f, cursorAlpha.toFloat())
                    GlStateManager.enableColorLogic()
                    GlStateManager.colorLogicOp(5387)
                    Renderer.drawRect((x + n) / textScale, (y + 2.0) / textScale, 1.0, height - 4.0)
                    GlStateManager.disableColorLogic()
                }
            }
            return
        }

        // TODO: finish this part
        RoundedRect.drawRoundedRect(
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            radius.toFloat()
        )
    }

    override fun onUnfocus(event: UIFocusEvent) = apply {
        cursorAnimation.stop()
        shouldBlink = false
    }

    override fun onKeyType(event: UIKeyType) = apply {
        val c = event.char
        val keycode = event.keycode
        var char = if (c in CharCategory.PRIVATE_USE) Char.MIN_VALUE else c
        when (keycode) {
            Keyboard.KEY_ESCAPE -> {
                focused = false
                propagateUnfocus(-1.0, -1.0)
            }
            Keyboard.KEY_BACK -> {
                text = text.substring(0, max(text.length - 1, 0))
                if (cursorPos != 0) cursorPos--
            }
            Keyboard.KEY_HOME -> {
                cursorPos = 0
            }
            Keyboard.KEY_END -> {
                cursorPos = text.length
            }
            Keyboard.KEY_RIGHT -> {
                if (cursorPos >= text.length) return@apply
                cursorPos++
            }
            Keyboard.KEY_LEFT -> {
                if (cursorPos == 0) return@apply
                cursorPos--
            }
            else -> {
                if (!isAllowedCharacter(char)) return@apply
                if (char == '\r') char = '\n'
                text += char.toString()
                if (cursorPos < text.length) cursorPos++
            }
        }
    }

    companion object {
        /**
         * * Checks whether the character is in a valid range
         * * Taken from mojang's `ChatAllowedCharacters` class
         * * @author Mojang
         */
        fun isAllowedCharacter(char: Char): Boolean = char.code != 167 && char >= ' ' && char.code != 127
    }
}
