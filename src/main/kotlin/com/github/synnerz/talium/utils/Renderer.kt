package com.github.synnerz.talium.utils

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DestFactor
import com.mojang.blaze3d.platform.SourceFactor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Formatting
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object Renderer {
    private val ALLOCATOR = BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE)
    private val emptyStack = MatrixStack().peek()
    private val QuadPipeline = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation("talium/pipeline1")
        .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
//        .withBlend(BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO))
//        .withCull(false)
        .build()
    private val QuadLayer = RenderLayer.of(
        "talium/layer1",
        1536,
        QuadPipeline,
        RenderLayer.MultiPhaseParameters
            .builder()
            .build(false)
    )
    val fontRenderer: TextRenderer by lazy { MinecraftClient.getInstance().textRenderer }
    private const val WHITE: Int = 0xFFFFFFFF.toInt()

    fun getBuffer(drawMode: DrawMode, format: VertexFormat) = BufferBuilder(ALLOCATOR, drawMode, format)

    @JvmOverloads
    fun drawLine(
        bufr: BufferBuilder,
        stack: MatrixStack.Entry,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        thickness: Float = 1f,
        color: Color
    ) {
        val theta = -atan2(y2 - y1, x2 - x1)
        val i = sin(theta) * (thickness / 2)
        val j = cos(theta) * (thickness / 2)

        bufr.vertex(stack, x1 + i, y1 + j, 0f).color(color.rgb)
        bufr.vertex(stack, x2 + i, y2 + j, 0f).color(color.rgb)
        bufr.vertex(stack, x2 - i, y2 - j, 0f).color(color.rgb)
        bufr.vertex(stack, x1 - i, y1 - j, 0f).color(color.rgb)
    }

    @JvmOverloads
    fun drawRect(
        x: Double, y: Double, width: Double, height: Double,
        solid: Boolean = true,
        color: Color = Color.WHITE,
        lineWidth: Float = 1f,
        stack: MatrixStack.Entry? = null
    ) {
        val bufr = getBuffer(DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        if (!solid) {
            val x1 = x.toFloat()
            val y1 = y.toFloat()
            val x2 = (x + width).toFloat()
            val y2 = (y + height).toFloat()
            // Top
            drawLine(bufr, stack ?: emptyStack, x1, y1, x2, y1, lineWidth, color)
            // Left
            drawLine(bufr, stack ?: emptyStack, x1, y1, x1, y2, lineWidth, color)
            // Right
            drawLine(bufr, stack ?: emptyStack, x2, y1, x2, y2, lineWidth, color)
            // Bottom
            drawLine(bufr, stack ?: emptyStack, x1, y2, x2, y2, lineWidth, color)

            val end = bufr.endNullable() ?: return
            QuadLayer.draw(end)

            return
        }

        bufr.vertex(stack ?: emptyStack, x.toFloat(), (y + height).toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack ?: emptyStack, (x + width).toFloat(), (y + height).toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack ?: emptyStack, (x + width).toFloat(), y.toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack ?: emptyStack, x.toFloat(), y.toFloat(), 0f).color(color.rgb)

        val end = bufr.endNullable() ?: return
        QuadLayer.draw(end)
    }

    @JvmOverloads
    fun drawInvertedColRect(x: Double, y: Double, width: Double, height: Double, alpha: Float = 255f) {
        if (width >= x || height >= y) return
//        GlStateManager.color(0f, 0f, 255f, alpha)
//        GlStateManager.enableColorLogic()
//        GlStateManager.colorLogicOp(GL11.GL_OR_REVERSE)
        drawRect(x, y, width, height)
//        GlStateManager.disableColorLogic()
    }

    fun Color.bind() {
//        RenderSystem.setShaderColor(
//            this.red.toFloat() / 255f,
//            this.green.toFloat() / 255f,
//            this.blue.toFloat() / 255f,
//            this.alpha.toFloat() / 255f
//        )
    }

    fun Color.withAlpha(alpha: Float): Color {
        return Color(
            this.red.toFloat() / 255f,
            this.green.toFloat() / 255f,
            this.blue.toFloat() / 255f,
            alpha / 255f
        )
    }

    fun Color.bind(alpha: Float) {
//        RenderSystem.setShaderColor(
//            this.red.toFloat(),
//            this.green.toFloat(),
//            this.blue.toFloat(),
//            alpha
//        )
    }

    fun Color.unbind() {
//        RenderSystem.setShaderColor(0f, 0f, 0f, 0f)
    }

    fun getMouseX(sr: ScaledResolution): Double =
        sr.mc.mouse.x * sr.mc.window.scaledWidth / max(1, sr.mc.window.width)

    fun getMouseY(sr: ScaledResolution): Double =
        sr.mc.mouse.y * sr.mc.window.scaledHeight / max(1, sr.mc.window.height)

    @JvmOverloads
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false, color: Int = WHITE, stack: MatrixStack.Entry? = null) {
        var _y = y
//        GlStateManager.enableTexture2D()
        val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
        text.split('\n').forEach {
            fontRenderer.draw(
                it,
                x,
                _y,
                color,
                shadow,
                stack?.positionMatrix ?: emptyStack.positionMatrix,
                immediate,
                TextRenderer.TextLayerType.NORMAL,
                0,
                0xf000f0,
            )
//            fontRenderer.drawString(it, x, _y, color, shadow)
            _y += fontRenderer.fontHeight
        }
//        GlStateManager.disableTexture2D()
    }

    fun String.trimToWidth(width: Double, scale: Float): String {
        var totalWidth = 0f
        var str = ""

        for (c in this) {
            str += c
            totalWidth += ("$c".getWidth() * scale)
            if (totalWidth >= width) break
        }

        return str
    }

    fun String.trimToWidth(width: Double): String = fontRenderer.trimToWidth(this, width.toInt())

    fun String.getWidth() = fontRenderer.getWidth(Formatting.strip(this))
}
