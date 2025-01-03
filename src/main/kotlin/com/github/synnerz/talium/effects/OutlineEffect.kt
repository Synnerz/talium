package com.github.synnerz.talium.effects

import com.github.synnerz.talium.shaders.ui.RoundedRectOutline
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import com.github.synnerz.talium.utils.Renderer.unbind
import java.awt.Color

/**
 * * Makes an outline-like effect surrounding the specified [component]
 */
open class OutlineEffect @JvmOverloads constructor(
    var width: Double = 1.0,
    var color: Color = Color(255, 255, 255, 255),
    var radius: Double = 0.0
) : UIEffect() {
    override fun preDraw() {
        if (component == null) return
        if (width == 0.0 || color.alpha == 0) return

        color.bind()

        if (radius == 0.0) {
            Renderer.drawRect(
                component!!.x - width,
                component!!.y - width,
                component!!.width + width * 2,
                component!!.height + width * 2
            )
            return
        }

        RoundedRectOutline.drawRoundedRectOutline(
            (component!!.x - width).toFloat(),
            (component!!.y - width).toFloat(),
            (component!!.width + width * 2).toFloat(),
            (component!!.height + width * 2).toFloat(),
            radius.toFloat(),
            width.toFloat()
        )

        color.unbind()
    }
}
