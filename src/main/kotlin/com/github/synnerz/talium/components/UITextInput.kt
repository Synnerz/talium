package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIFocusEvent
import com.github.synnerz.talium.events.UIKeyType
import com.github.synnerz.talium.utils.MathLib
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.getWidth
import com.github.synnerz.talium.utils.Renderer.trimToWidth
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
    var text: String = "",
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var cursorAnimation: Animation = Animation(Animations.CIRC_IN_OUT, 750f)
    var cursorPos: Int = 0
    var textScale: Float = 1f
    var cursorAlpha: Double = 255.0
    var shouldBlink: Boolean = false
    var selectionPos: Int = 0
    open var maxLength: Int = 0
    open var currentOffset: Int = 0

    override fun preDraw() = cursorAnimation.preDraw()

    open fun getSelectionLeft() = min(selectionPos, cursorPos).coerceIn(0, text.length)

    open fun getSelectionRight() = max(selectionPos, cursorPos).coerceIn(0, text.length)

    open fun getSelectedText() = text.substring(getSelectionLeft(), getSelectionRight())

    open fun getCurrentText() = text.substring(currentOffset).trimToWidth(width - 8.0, textScale)

    open fun deleteText(from: Int, to: Int): String {
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

    open fun getPreviousWord(): Int {
        var length = 0
        var inWord = false

        for (i in cursorPos - 1 downTo 0) {
            if (text[i] != ' ') inWord = true
            else if (inWord && text[i] == ' ') return length
            length++
        }

        return length
    }

    open fun getNextWord(): Int {
        var length = 0
        var inWord = false

        for (i in cursorPos until text.length) {
            if (text[i] != ' ') inWord = true
            else if (inWord && text[i] == ' ') return length
            length++
        }

        return length
    }

    open fun updateOffset() {
        currentOffset = min(currentOffset, cursorPos)
        val str = text.substring(currentOffset, cursorPos)
        val currWidth = str.getWidth() * textScale
        if (currWidth > (width - 8.0)) {
            val nw = str.trimToWidth((width - 8.0), textScale).length
            currentOffset = (cursorPos - nw).coerceAtLeast(0)
        }
    }

    override fun render() {
        updateOffset()
        UIRect.drawRect(x, y, width, height, radius)

        if (textScale != 1f) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(textScale, textScale, 0f)
        }

        val textHeight = 9f * textScale
        val heightCenter = (height - textHeight) / 2.0
        val ctext = getCurrentText()
        Renderer.drawString(
            ctext,
            (x.toFloat() + 2f) / textScale,
            (y + heightCenter).toFloat() / textScale
        )

        if (focused) {
            val maxSelectHeight = "A".getWidth() + 2.0

            if (cursorAnimation.shouldAnimate) {
                val ease = cursorAnimation.getEase()
                val newMin = if (cursorAlpha == 255.0) 255.0 else 0.0
                val newMax = if (cursorAlpha == 255.0) 0.0 else 255.0
                cursorAlpha = MathLib.rescale(ease, 0.0, 1.0, newMin, newMax)
                shouldBlink = ease >= 1.4f
            } else cursorAnimation.start()

            val n = (ctext.substring(
                0,
                (cursorPos - currentOffset).coerceIn(0, ctext.length)
            ).getWidth() + 2f) * textScale
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
                    maxSelectHeight,
                    cursorAlpha.toFloat()
                )
            }

            // Drawing selection
            val right = ctext.substring(
                (getSelectionLeft() - currentOffset).coerceIn(0, ctext.length),
                (getSelectionRight() - currentOffset).coerceIn(0, ctext.length)
            ).getWidth().toDouble()
            val left = ctext.substring(
                0,
                (getSelectionLeft() - currentOffset).coerceIn(0, ctext.length)
            ).getWidth().toDouble() * textScale

            Renderer.drawInvertedColRect(
                (x + 2.0 + left) / textScale,
                (y + heightCenter) / textScale,
                right,
                maxSelectHeight
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

    open fun write(str: String) {
        for (c in str.toCharArray()) {
            write(c)
        }
    }

    open fun write(c: Char) {
        if (!isAllowedCharacter(c)) return
        if (selectionPos != cursorPos) deleteText()
        // Avoid adding to the string if the text length is over the maximum length
        // if maxLength is `0` that means it's infinite, so we have to check whether
        // this variable is set to infinite or not
        if (maxLength != 0 && text.length >= maxLength) return

        var char = c
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
            Keyboard.KEY_ESCAPE -> {
                focused = false
                unfocus()
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
                if (cursorPos != text.length) {
                    if (isCtrl) cursorPos += getNextWord()
                    else cursorPos++
                }
            }
            Keyboard.KEY_LEFT -> {
                if (cursorPos != 0) {
                    if (isCtrl) cursorPos -= getPreviousWord()
                    else cursorPos--
                }
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
