package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIFocusEvent
import com.github.synnerz.talium.events.UIKeyType
import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.MathLib
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import kotlin.math.max
import kotlin.math.min

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
    var shouldBlink: Boolean = false
    var selectionPos: Int = 0

    override fun preDraw() {
        cursorAnimation.preDraw()
    }

    fun getSelectionLeft(): Int {
        return min(selectionPos, cursorPos).coerceIn(0, text.length)
    }

    fun getSelectionRight(): Int {
        return max(selectionPos, cursorPos).coerceIn(0, text.length)
    }

    open fun getSelectedText()
        = text.substring(getSelectionLeft(), getSelectionRight())

    fun deleteText(from: Int, to: Int): String {
        if (from >= to || from < 0 || to > text.length) return ""
        val deleted = text.substring(from, to)
        text = text.substring(0, from) + text.substring(to)
        return deleted
    }

    open fun deleteText() {
        if (cursorPos == selectionPos && cursorPos > 0) {
            text = text.substring(0, cursorPos - 1) + text.substring(cursorPos)
            cursorPos--
            selectionPos = cursorPos
            return
        }
        val from = getSelectionLeft()
        val to = getSelectionRight()

        deleteText(from, to)
        cursorPos = from
        selectionPos = from
    }

    override fun render() {
        // TODO: add wrapped text or scissors effect to not go off bound
        when (radius) {
            0.0 -> {
                Renderer.drawRect(x, y, width, height)
            }
            else -> {
                RoundedRect.drawRoundedRect(
                    x.toFloat(),
                    y.toFloat(),
                    width.toFloat(),
                    height.toFloat(),
                    radius.toFloat()
                )
            }
        }

        if (textScale != 1f) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(textScale, textScale, 0f)
        }

        val textHeight = 9f * textScale
        val heightCenter = (height - textHeight) / 2.0
        Renderer.drawString(
            text,
            (x.toFloat() + 2f) / textScale,
            (y + heightCenter).toFloat() / textScale
        )

        if (focused) {
            if (cursorAnimation.shouldAnimate) {
                val ease = cursorAnimation.getEase()
                val newMin = if (cursorAlpha == 255.0) 255.0 else 0.0
                val newMax = if (cursorAlpha == 255.0) 0.0 else 255.0
                cursorAlpha = MathLib.rescale(ease, 0.0, 1.0, newMin, newMax)
                shouldBlink = ease >= 1.4f
            } else cursorAnimation.start()

            val n = (text.substring(0, cursorPos).getWidth() + 2f) * textScale
            // Check whether the current [cursorPos] is equals to the max string length
            // this means that the cursor is at the end, so we can change the rendering to be `_` instead of `|`
            if (cursorPos == text.length) {
                // Scuffed blinking but it works i guess
                if (!shouldBlink)
                    Renderer.drawString(
                        "_",
                        (x.toFloat() + n) / textScale,
                        (y + heightCenter).toFloat() / textScale,
                        color = 14737632)
            }
            // Else we render the blinking `|`
            else {
                Renderer.drawInvertedColRect(
                    (x + n) / textScale,
                    (y + heightCenter) / textScale,
                    1.0,
                    height - 4.0,
                    cursorAlpha.toFloat()
                )
            }

            // Drawing selection
            val right = getSelectedText().getWidth().toDouble()
            val left = text.substring(0, getSelectionLeft()).getWidth().toDouble() * textScale
            Renderer.drawInvertedColRect(
                (x + 2.0 + left) / textScale,
                (y + heightCenter) / textScale,
                right,
                height - 4.0
            )
        }
        if (textScale != 1f) {
            GlStateManager.popMatrix()
        }
    }

    override fun onUnfocus(event: UIFocusEvent) = apply {
        cursorAnimation.stop()
        shouldBlink = false
        cursorPos = text.length
        selectionPos = cursorPos
    }
    // TODO: mouse to cursor position for cursor selection when clicking on the text input
    // TODO: mouse drag selection to select text whenever the user drags the mouse on the text input

    fun write(str: String) {
        for (c in str.toCharArray()) {
            write(c)
        }
    }

    fun write(c: Char) {
        if (!isAllowedCharacter(c)) return
        if (selectionPos != cursorPos) deleteText()
        var char = c
        // TODO: add maxLength check
        if (char == '\r') char = '\n'
        text = text.substring(0, cursorPos) + char + text.substring(cursorPos)
        cursorPos++
        selectionPos = cursorPos
    }

    override fun onKeyType(event: UIKeyType) = apply {
        val c = event.char
        val keycode = event.keycode
        val char = if (c in CharCategory.PRIVATE_USE) Char.MIN_VALUE else c
        val isShifting = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
        val isCtrl = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)

        if (isCtrl) {
            when (keycode) {
                Keyboard.KEY_A -> {
                    cursorPos = 0
                    selectionPos = text.length
                    return@apply
                }
                Keyboard.KEY_C -> {
                    GuiScreen.setClipboardString(getSelectedText())
                    return@apply
                }
                Keyboard.KEY_V -> {
                    write(GuiScreen.getClipboardString())
                    return@apply
                }
                Keyboard.KEY_X -> {
                    GuiScreen.setClipboardString(getSelectedText())
                    deleteText()
                    return@apply
                }
                // TODO: maybe do undo but not important
            }
        }

        when (keycode) {
            // TODO: next word (holding ctrl) jumps
            Keyboard.KEY_ESCAPE -> {
                focused = false
                propagateUnfocus(-1.0, -1.0)
            }
            Keyboard.KEY_BACK -> {
                deleteText()
                return@apply
            }
            Keyboard.KEY_DELETE -> {
                deleteText()
                return@apply
            }
            Keyboard.KEY_HOME -> {
                cursorPos = 0
            }
            Keyboard.KEY_END -> {
                cursorPos = text.length
            }
            Keyboard.KEY_RIGHT -> {
                if (cursorPos != text.length) cursorPos++
            }
            Keyboard.KEY_LEFT -> {
                if (cursorPos != 0) cursorPos--
            }
            else -> {
                write(char)
                return@apply
            }
        }

        if (!isShifting) selectionPos = cursorPos
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
