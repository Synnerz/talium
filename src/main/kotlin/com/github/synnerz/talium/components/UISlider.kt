package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIDragEvent
import com.github.synnerz.talium.events.UIKeyType
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import com.github.synnerz.talium.utils.Renderer.getWidth
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.min
import kotlin.math.roundToInt

open class UISlider @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Double = 0.0,
    var min: Double,
    var max: Double,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open var thumb = UISliderThumb(15.0, this)
    open var completion = UISliderCompletion(this)
    open var textScale: Float = 1f
    /**
     * * Field that decides how many steps it'll take per arrow key press
     * * i.e. left arrow: slider goes down once since `1` is default
     */
    open var keyStep: Int = 1
    /**
     * * Field that decides how many steps it'll take per control/ctrl + arrow key press
     * * i.e. left arrow: slider goes down twice since `2` is default
     */
    open var ctrlStep: Int = 2

    override fun render() {
        // TODO: make decimal slider or adjust this one to work like it
        // Main bar
        UIRect.drawRect(x, y, width, height, radius)
        val handleX = min((value - min) / (max - min) * width, width)
        // Completion bar
        completion.bgColor.bind()
        UIRect.drawRect(x, y, handleX, height, radius)
        // Thumb
        thumb.bgColor.bind()
        val thumbX = (x + handleX - (thumb.width / 2.0)).coerceIn(x, (x + width) - thumb.width)
        val thumbY = y - 2
        val thumbWidth = thumb.width.coerceIn(0.0, width)
        val thumbHeight = height + 4
        UIRect.drawRect(thumbX, thumbY, thumbWidth, thumbHeight, radius)
        // Text inside thumb
        val text = "${value.roundToInt()}"
        val textOffset = (thumbWidth - (text.getWidth() * textScale)) / 2.0
        val textHeight = 9f * textScale
        if (textScale != 1f) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(textScale, textScale, 0f)
        }
        Renderer.drawString(
            text,
            (thumbX + textOffset).toFloat() / textScale,
            (thumbY + (thumbHeight - textHeight) / 2.0).toFloat() / textScale
        )
        if (textScale != 1f) {
            GlStateManager.popMatrix()
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        setCurrentX(event.x)
    }

    override fun onMouseDragOut(event: UIDragEvent) = apply {
        if (!focused) return@apply

        setCurrentX(event.x)
    }

    override fun onKeyType(event: UIKeyType) = apply {
        val isCtrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
        when (event.keycode) {
            Keyboard.KEY_LEFT -> {
                if (isCtrl) setCurrentValue(value - ctrlStep)
                else setCurrentValue(value - keyStep)
            }
            Keyboard.KEY_RIGHT -> {
                if (isCtrl) setCurrentValue(value + ctrlStep)
                else setCurrentValue(value + keyStep)
            }
            Keyboard.KEY_HOME -> {
                setCurrentValue(min)
            }
            Keyboard.KEY_END -> {
                setCurrentValue(max)
            }
        }
    }

    open fun setCurrentX(x: Double) {
        val rx = x - this.x
        value = ((rx.toFloat() / width) * (max - min) + min).coerceIn(min, max)
    }

    open fun setCurrentValue(value: Double) {
        this.value = value.coerceIn(min, max)
    }
}

open class UISliderThumb(
    _width: Double,
    parent: UIBase? = null
) : UIBase(0.0, 0.0, _width, 0.0, parent) {
    override var bgColor: Color = Color(140, 140, 140, 80)
}

open class UISliderCompletion(
    parent: UIBase? = null
) : UIBase(0.0, 0.0, 0.0, 0.0, parent) {
    override var bgColor: Color = Color(140, 140, 140, 80)
}
