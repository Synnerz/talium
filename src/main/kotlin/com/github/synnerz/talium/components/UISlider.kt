package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIDragEvent
import com.github.synnerz.talium.events.UIKeyType
import com.mojang.blaze3d.platform.InputConstants
import java.awt.Color
import kotlin.math.min
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

    open fun getCurrentValue(): Double = value

    open fun getDisplayValue() = "${value.roundToInt()}"

    override fun render() {
        // Main bar
        UIRect.drawRect(x, y, width, height, radius, bgColor)

        // Completion bar
        val handleX = ((value - min) / (max - min)).coerceIn(0.0, 1.0) * width
        UIRect.drawRect(x, y, handleX, height, radius, completion.bgColor)

        // Thumb
        val thumbWidth = thumb.width.coerceIn(0.0, width)
        val thumbX = x + handleX - (thumbWidth / 2.0)
        val thumbY = y - 2
        val thumbHeight = height + 4
        UIRect.drawRect(thumbX, thumbY, thumbWidth, thumbHeight, radius, thumb.bgColor)

        // Text inside thumb
        UIText.drawCenteredText(getDisplayValue(), thumbX, thumbY, thumbWidth, thumbHeight, textScale)
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        setCurrentX(event.x)
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!focused) return@apply

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
