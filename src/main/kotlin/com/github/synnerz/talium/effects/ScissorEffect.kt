package com.github.synnerz.talium.effects

import com.github.synnerz.talium.components.UIBase
import org.lwjgl.opengl.GL11

/**
 * * Enables scissor effect to be bound to the specified [component]'s bounds
 */
open class ScissorEffect(
    component: UIBase
) : UIEffect(component) {
    override fun preDraw() {
        if (component == null) return

        val scaleFactor = component.scaledResolution?.scaleFactor ?: 1
        val ( x1, y1, x2, y2 ) = component.bounds
        if (x1 == -1.0) return

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(
            x1.toInt() * scaleFactor,
            ((component.scaledResolution?.scaledHeight ?: 0) - y2.toInt()) * scaleFactor,
            (x2 - x1).toInt() * scaleFactor,
            (y2 - y1).toInt() * scaleFactor
        )
    }

    override fun postDraw() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }
}