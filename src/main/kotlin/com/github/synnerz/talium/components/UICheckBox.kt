package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.utils.MathLib
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import com.github.synnerz.talium.utils.Renderer.getWidth
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.StringUtils
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
    open var check = "§l✓"
    override var xAnimation: Animation? = Animation(Animations.QUAD_IN_OUT, 5000f)
    open var currentAlpha: Double = 255.0

    override fun render() {
        if (xAnimation?.shouldAnimate == true) {
            val ease = xAnimation!!.getEase()
            val newMin = if (currentAlpha == 255.0) 255.0 else 0.0
            val newMax = if (currentAlpha == 255.0) 0.0 else 255.0
            currentAlpha = MathLib.rescale(ease, 0.0, 1.0, newMin, newMax)
        }

        if (value) enabledColor.bind(currentAlpha.toFloat())
        else disabledColor.bind(currentAlpha.toFloat())

        UIRect.drawRect(x, y, width, height, radius)

        // Draw checkmark
        if (value) {
            if (textScale != 1f) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(textScale, textScale, 0f)
            }

            val textHeight = 9f * textScale
            val checkWidth = StringUtils.stripControlCodes(check).getWidth() * textScale
            Renderer.drawString(
                check,
                (x + (width - checkWidth) / 2.0).toFloat() / textScale,
                (y + (height - textHeight) / 2.0).toFloat() / textScale
            )

            if (textScale != 1f) GlStateManager.popMatrix()
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        value = !value
        xAnimation?.start()
    }
}
