package com.github.synnerz.talium.effects

import com.github.synnerz.talium.utils.Renderer.bind
import com.github.synnerz.talium.utils.Renderer.unbind
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * * Makes an inverse-like color effect using the color that is behind this component as its color
 * * i.e. component behind is red then this one will be blue
 */
open class InverseEffect() : UIEffect(null) {
    private val color = Color(1f, 1f, 1f, 1f)
    override var forceColor: Boolean = true

    override fun preDraw() {
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(775, 0, 1, 0)
        color.bind()
    }

    override fun postDraw() {
        GlStateManager.disableBlend()
        color.unbind()
    }
}