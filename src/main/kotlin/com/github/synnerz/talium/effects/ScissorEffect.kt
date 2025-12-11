package com.github.synnerz.talium.effects

import com.github.synnerz.talium.components.UIElement
import com.github.synnerz.talium.utils.Renderer
import kotlin.math.ceil

/**
 * * Enables scissor effect to be bound to the specified [component]'s bounds
 */
open class ScissorEffect : UIEffect() {
    override fun preDraw() {
        if (component == null) return

        enableScissor(component!!)
    }

    override fun postDraw() = disableScissor()

    companion object {
        var scissorState: Boolean = false

        fun enableScissor(comp: UIElement)
            = enableScissor(comp.x, comp.y, comp.width, comp.height)

        fun enableScissor(x: Double, y: Double, width: Double, height: Double) {
            if (x == -1.0) return

            Renderer.scissorStack.push(x.toInt(), y.toInt(), ceil(width).toInt(), ceil(height).toInt())
            scissorState = true
        }

        fun disableScissor() {
            if (!scissorState) return

            Renderer.scissorStack.pop()
            scissorState = false
        }
    }
}