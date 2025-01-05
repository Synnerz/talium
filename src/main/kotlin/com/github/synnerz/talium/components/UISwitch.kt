package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.shaders.ui.RoundedRect
import com.github.synnerz.talium.utils.MathLib.rescale
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import java.awt.Color

open class UISwitch @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    var state: Boolean = false,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var enabledColor: Color? = null
    var knob = UIKnobSwitch(radius = radius)
    // TODO: make animation be handled in the [UIBase] component
    var xAnimation = Animation(Animations.QUAD_IN_OUT)

    override fun onUpdate() = apply {
        knob.x = -1.0
        // knob.height = -1.0
    }

    override fun render() {
        if (enabledColor == null) enabledColor = bgColor.brighter()
        if (state) enabledColor!!.bind()
        else bgColor.bind()
        // TODO: remove this whenever [UIBase] handles animations
        xAnimation.render()
        // Draw the background rect
        if (radius == 0.0) Renderer.drawRect(x, y, width, height)
        else RoundedRect.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

        // Draw the knob
        if (knob.x == -1.0) {
            if (state) knob.x = x + 2 + (width - (width / 5)) - 4
            else knob.x = x + 2
        }
        if (knob.height <= 0.0) knob.height = height / 2

        if (xAnimation.shouldAnimate) {
            val ease = xAnimation.getEase()

            knob.x =
                if (state) rescale(ease, 0.0, 1.0, knob.x, x + 2 + (width - (width / 5)) - 4)
                else rescale(ease, 0.0, 1.0, knob.x, x + 2)
        }

        if (state) knob.enabledColor.bind()
        else knob.disabledColor.bind()

        knob.y = y + (knob.height / 2)
        knob.width = width / 5

        if (knob.radius == 0.0) Renderer.drawRect(knob.x, knob.y, knob.width, knob.height)
        else {
            RoundedRect.drawRoundedRect(
                knob.x.toFloat(),
                knob.y.toFloat(),
                knob.width.toFloat(),
                knob.height.toFloat(),
                knob.radius.toFloat())
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        state = !state
        xAnimation.start()
    }
}

// TODO: make this a component or something as well as making the height % based
open class UIKnobSwitch @JvmOverloads constructor(
    var height: Double = -1.0,
    var radius: Double = 0.0
) {
    var enabledColor: Color = Color(0, 255, 0,  255)
    var disabledColor: Color = Color(255, 0, 0,  255)
    var x: Double = -1.0
    var y: Double = 0.0
    var width: Double = 0.0
}
