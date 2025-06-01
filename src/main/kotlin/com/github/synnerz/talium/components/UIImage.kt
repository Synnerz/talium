package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

open class UIImage @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var image: BufferedImage? = null,
    var scale: Float = 1f,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var bgColor: Color = Color(255, 255, 255, 255)
    var imgUploaded: Boolean = false
    val textureWidth = image?.width ?: 0.0
    val textureHeight = image?.height ?: 0.0
    var dynamicTexture: DynamicTexture? = null

    override fun render() {
        if (!imgUploaded) {
            imgUploaded = true
            dynamicTexture = DynamicTexture(image)
            return
        }
        if (dynamicTexture == null) return

        GlStateManager.scale(scale, scale, 50f)
        GlStateManager.bindTexture(dynamicTexture!!.glTextureId)
        GlStateManager.enableTexture2D()

        val dx = x / scale
        val dy = y / scale

        Renderer.worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        Renderer.worldRenderer.pos(dx, dy + height, 0.0).tex(0.0, 1.0).endVertex()
        Renderer.worldRenderer.pos(dx + width, dy + height, 0.0).tex(1.0, 1.0).endVertex()
        Renderer.worldRenderer.pos(dx + width, dy, 0.0).tex(1.0, 0.0).endVertex()
        Renderer.worldRenderer.pos(dx, dy, 0.0).tex(0.0, 0.0).endVertex()
        Renderer.tessellator.draw()

        GlStateManager.disableTexture2D()
        GlStateManager.scale(1f / scale, 1f / scale, 1f / 50f)
    }

    /**
     * - NOTE: You MUST call this once you no longer need this image
     * so it can be properly deleted from GPU and not cause memory leak
     */
    open fun destroy() {
        dynamicTexture?.deleteGlTexture()
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun fromFile(x: Double, y: Double, width: Double, height: Double, file: File, scale: Float = 1f, parent: UIBase? = null): UIImage {
            return UIImage(x, y, width, height, ImageIO.read(file), scale, parent)
        }
    }
}