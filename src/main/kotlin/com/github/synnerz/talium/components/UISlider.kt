package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UICharEvent
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIDragEvent
import com.github.synnerz.talium.events.UIFocusEvent
import com.github.synnerz.talium.events.UIKeyType
import com.github.synnerz.talium.utils.Renderer
import com.mojang.blaze3d.platform.InputConstants
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.math.roundToLong

open class UISlider @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Double = 0.0,
    var min: Double = 0.0,
    var max: Double = 1.0,
    var radius: Double = 0.0,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open var thumb = UISliderThumb(15.0, this)
    open var completion = UISliderCompletion(this)
    open var input = UISliderInput(this).apply {
        hide()
        text = getDisplayValue()
    }
    open var textScale: Float = 1f
    open var valueStep: Double = Double.MIN_VALUE
    /**
     * * Field that decides how many steps it'll take per arrow key press
     * * i.e. left arrow: slider goes down once since `1` is default
     */
    open var keyStep: Double = 1.0
    /**
     * * Field that decides how many steps it'll take per control/ctrl + arrow key press
     * * i.e. left arrow: slider goes down twice since `2` is default
     */
    open var ctrlStep: Double = 2.0
    /**
     * * Whether right click was triggered on the slider for special text input
     */
    open var inputFocused = false
    /**
     * * Adds this amount of width to the input itself after doing thumbWidth
     * * this is to ensure that the user can see more than 1 letter in the input
     */
    open var inputWidthOffset = 15.0

    open fun getCurrentValue(): Double = value

    open fun getDisplayValue() = "${value.roundToInt()}"

    open fun validateString(str: String): Boolean = str.all { it.isDigit() }

    open fun onInputEnter(str: String) {
        val input = str.toDoubleOrNull() ?: return

        setCurrentValue(input)
    }

    override fun render() {
        // Main bar
        UIRect.drawRect(x, y, width, height, radius, bgColor)

        // Completion bar
        val handleX = ((value - min) / (max - min)).coerceIn(0.0, 1.0) * width
        UIRect.drawRect(x, y, handleX, height, radius, completion.bgColor)
        if (inputFocused) return

        // Thumb
        val thumbWidth = thumb.width.coerceIn(0.0, width)
        val thumbX = x + handleX - (thumbWidth / 2.0)
        val thumbY = y - 2
        val thumbHeight = height + 4

        UIRect.drawRect(thumbX, thumbY, thumbWidth, thumbHeight, radius, thumb.bgColor)

        // Text inside thumb
        UIText.drawCenteredText(getDisplayValue(), thumbX, thumbY, thumbWidth, thumbHeight, textScale)
    }

    override fun preChildDraw() {
        val handleX = ((value - min) / (max - min)).coerceIn(0.0, 1.0) * width
        val thumbWidth = thumb.width.coerceIn(0.0, width) + inputWidthOffset
        val thumbX = x + handleX - (thumbWidth / 2.0)
        val thumbY = y - 2
        val thumbHeight = height + 4

        if (!inputFocused || input.scaledResolution == null) return

        input.x = thumbX
        input.y = thumbY
        input.width = thumbWidth
        input.height = thumbHeight
        input.bounds = UIElement.Boundaries(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbHeight)
    }

    override fun onMouseDown(event: UIClickEvent) = apply {
        if (event.button == Renderer.RMBTN) {
            inputFocused = !inputFocused
            if (inputFocused) {
                input.focus()
                input.unhide()
            } else {
                input.unfocus()
                input.hide()
            }
            return@apply
        }
        if (event.button != Renderer.LMBTN) return@apply
        setCurrentX(event.x)
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!focused || inputFocused) return@apply

        setCurrentX(event.currX)
    }

    override fun onKeyType(event: UIKeyType) = apply {
        val isCtrl =
            UITextInput.isKeyDown(InputConstants.KEY_LCONTROL) ||
            UITextInput.isKeyDown(InputConstants.KEY_RCONTROL)

        when (event.keycode) {
            InputConstants.KEY_LEFT -> {
                if (isCtrl) setCurrentValue(value - ctrlStep)
                else setCurrentValue(value - keyStep)
            }
            InputConstants.KEY_RIGHT -> {
                if (isCtrl) setCurrentValue(value + ctrlStep)
                else setCurrentValue(value + keyStep)
            }
            InputConstants.KEY_HOME -> {
                setCurrentValue(min)
            }
            InputConstants.KEY_END -> {
                setCurrentValue(max)
            }
        }
    }

    override fun onUnfocus(event: UIFocusEvent) = apply {
        inputFocused = false
        input.unfocus()
        input.hide()
    }

    fun setCurrentX(x: Double) {
        val rx = x - this.x
        val v = (rx / width).coerceIn(0.0, 1.0) * (max - min)
        val mul = v / valueStep
        setCurrentValue(
            if (valueStep == Double.MIN_VALUE || mul.isInfinite()) v + min
            else mul.roundToLong() * valueStep + min
        )
    }

    open fun setCurrentValue(value: Double) {
        this.value = value.coerceIn(min, max)
        input.text = getDisplayValue()
    }
}

open class UISliderThumb(
    _width: Double,
    parent: UIElement? = null
) : UIBase(0.0, 0.0, _width, 0.0, parent) {
    override var bgColor: Color = Color(140, 140, 140, 80)
}

open class UISliderCompletion(
    parent: UIElement? = null
) : UIBase(0.0, 0.0, 0.0, 0.0, parent) {
    override var bgColor: Color = Color(140, 140, 140, 80)
}

open class UISliderInput(
    parent: UIElement? = null
) : UITextInput(0.0, 0.0, 0.0, 0.0, "", parent = parent) {
    override var bgColor: Color = Color(140, 140, 140, 80)

    override fun onKeyType(event: UIKeyType) = apply {
        super.onKeyType(event)

        if (event.keycode != InputConstants.KEY_RETURN) return@apply
        val slider = parent as? UISlider ?: return@apply

        unfocus()
        hide()
        slider.inputFocused = false
        slider.onInputEnter(text)
    }

    override fun onCharType(event: UICharEvent) = apply {
        val slider = parent as? UISlider ?: return@apply
        if (!slider.validateString(event.str)) return@apply

        super.onCharType(event)
    }

    override fun onFocus(event: UIFocusEvent) = apply {
        super.onFocus(event)

        cursorPos = 0
        selectionPos = text.length
    }

    override fun onUnfocus(event: UIFocusEvent) = apply {
        super.onUnfocus(event)

        val slider = parent as? UISlider ?: return@apply
        if (!slider.inputFocused) return@apply

        hide()
        slider.inputFocused = false
    }
}
