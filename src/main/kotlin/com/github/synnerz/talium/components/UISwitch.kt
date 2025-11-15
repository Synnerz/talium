package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.MathLib.rescale
import com.github.synnerz.talium.utils.Renderer
import java.awt.Color

/**
 * * Switch component that handles the state as well as the animation and colors
 */
open class UISwitch @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var state: Boolean = false,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open var enabledColor: Color? = null
    open var knob = UIKnobSwitch(radius = radius, parent = this)
        set(value) {
            // Make sure that the [knob] has [this] switch as a parent
            // In case the [UIKnobSwitch] was miss-constructed
            if (!value.hasParent()) value.setChildOf(this)
            field = value
        }
    open var initial: Boolean = true
    override var xAnimation: Animation? = Animation(Animations.QUAD_IN_OUT)

    override fun onUpdate() = apply {
        initial = true
    }

    override fun render() {
        if (enabledColor == null) enabledColor = bgColor.brighter()

        // Draw the background rect
        if (radius == 0.0) Renderer.drawRect(x, y, width, height, color = if (state) enabledColor!! else bgColor)
        else RoundedRect.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

        // Draw the knob
        if (initial) {
            if (state) {
                knob._x = 80.0
                knob.x = x + 2 + (width - (width / 5)) - 4
            }
            else {
                knob._x = 1.0
                knob.x = x + 2
            }
        }
        if (knob.height <= 0.0) knob.height = height / 2

        if (xAnimation!!.shouldAnimate) {
            val ease = xAnimation!!.getEase()

            knob.x =
                if (state) rescale(ease, 0.0, 1.0, knob.x, x + 2 + (width - (width / 5)) - 4)
                else rescale(ease, 0.0, 1.0, knob.x, x + 2)
        }

        knob.y = y + (height - knob.height) * 0.5f
        knob.width = width / 5

        if (knob.radius == 0.0)
            Renderer.drawRect(
                knob.x.coerceIn(x + 2, x + 2 + (width - (width / 5)) - 4),
                knob.y,
                knob.width,
                knob.height,
                color = if (state) knob.enabledColor else knob.disabledColor
            )
        else
            RoundedRect.drawRoundedRect(
                knob.x.toFloat(),
                knob.y.toFloat(),
                knob.width.toFloat(),
                knob.height.toFloat(),
                knob.radius.toFloat())

        initial = false
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply
        state = !state
        xAnimation!!.start()
    }
}

/**
 * * This is the `knob` of the [UISwitch] component.
 * * Only the [_height] is taken into consideration since it centers the knob based off of that
 * inside the [UISwitch] component
 * * You can change the [enabledColor] and/or [disabledColor]
 * @param _height Height in percent `0-100`
 */
open class UIKnobSwitch @JvmOverloads constructor(
    _height: Double = -1.0,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(0.0, 0.0, 0.0, _height, parent) {
    /** * The color of this `knob` whenever the [UISwitch.state] is `true` */
    open var enabledColor: Color = Color(0, 255, 0,  255)
    /** * The color of this `knob` whenever the [UISwitch.state] is `false` */
    open var disabledColor: Color = Color(255, 0, 0,  255)
}
