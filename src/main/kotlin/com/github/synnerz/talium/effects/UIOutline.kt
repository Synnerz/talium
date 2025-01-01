package com.github.synnerz.talium.effects

import com.github.synnerz.talium.components.UIBase
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import com.github.synnerz.talium.utils.Renderer.unbind
import java.awt.Color

open class UIOutline @JvmOverloads constructor(
    var width: Double = 1.0,
    var color: Color = Color(255, 255, 255, 255),
    var radius: Double = 0.0,
    component: UIBase
) : UIEffect(component) {
    override fun preDraw() {
        if (component == null) return
        if (width == 0.0 || color.alpha == 0) return

        color.bind()
        if (radius == 0.0) {
            Renderer.drawRect(
                component.x - width,
                component.y - width,
                component.width + width * 2,
                component.height + width * 2
            )
            return
        }
        // TODO: make the rounded rect logic here
        // whenever shaders are done
    }

    override fun postDraw() {
        color.unbind()
    }
}