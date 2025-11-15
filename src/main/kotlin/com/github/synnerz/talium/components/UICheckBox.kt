package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.utils.MathLib
import com.github.synnerz.talium.utils.Renderer.withAlpha
import java.awt.Color

open class UICheckBox @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Boolean = false,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open var enabledColor: Color = Color(0, 255, 0)
    open var disabledColor: Color = Color(255, 0, 0)
    open var textScale: Float = 1f
    /** * The checkmark as a string */
    open var check = "§l✔"
    open var cross = "§l✘"
    override var xAnimation: Animation? = Animation(Animations.QUAD_IN, 1500f)
    open var currentAlpha: Double = 255.0

    override fun render() {
        if (xAnimation?.shouldAnimate == true) {
            val ease = xAnimation!!.getEase()
            val newMin = if (currentAlpha == 255.0) 255.0 else 0.0
            val newMax = if (currentAlpha == 255.0) 0.0 else 255.0
            currentAlpha = MathLib.rescale(ease, 0.0, 1.0, newMin, newMax)
        }

        val checkColor =
            if (value) enabledColor
            else disabledColor

        UIRect.drawRect(x, y, width, height, radius, bgColor)
        if (currentAlpha < 20.0) return

        // Draw checkmark/crossmark
        if (value) UIText.drawCenteredText(check, x, y, width, height, textScale, color = checkColor.withAlpha(currentAlpha.toFloat()))
        else UIText.drawCenteredText(cross, x, y, width, height, color = checkColor.withAlpha(currentAlpha.toFloat()))
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply
        value = !value
        xAnimation?.start()
    }
}
