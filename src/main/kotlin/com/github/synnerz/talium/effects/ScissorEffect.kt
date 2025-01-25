package com.github.synnerz.talium.effects

import com.github.synnerz.talium.components.UIBase
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11

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

        fun enableScissor(comp: UIBase)
            = enableScissor(comp.x, comp.y, comp.width, comp.height, comp.scaledResolution)

        fun enableScissor(x: Double, y: Double, width: Double, height: Double, sr: ScaledResolution?) {
            if (x == -1.0) return
            val scale = sr?.scaleFactor ?: 1
            val scaledHeight = sr?.scaledHeight ?: 0

            if (!scissorState) GL11.glEnable(GL11.GL_SCISSOR_TEST)
            val y2 = (y + height).toInt()
            GL11.glScissor(
                x.toInt() * scale,
                (scaledHeight - y2) * scale,
                width.toInt() * scale,
                height.toInt() * scale
            )
            scissorState = true
        }

        fun disableScissor() {
            if (!scissorState) return

            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            scissorState = false
        }
    }
}