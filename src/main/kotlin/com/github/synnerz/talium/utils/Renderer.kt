package com.github.synnerz.talium.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
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
    private val QuadPipeline = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation("talium/pipeline1")
        .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
        .build()
    private val QuadLayer = RenderLayer.of(
        "talium/layer1",
        1536,
        QuadPipeline,
        RenderLayer.MultiPhaseParameters
            .builder()
            .build(false)
    )
    private val TextHighlightLayer = RenderLayer.of(
        "talium/layer2",
        1536,
        RenderPipelines.GUI_TEXT_HIGHLIGHT,
        RenderLayer.MultiPhaseParameters
            .builder()
            .build(false)
    )
    val fontRenderer: TextRenderer by lazy { MinecraftClient.getInstance().textRenderer }
    private const val WHITE: Int = 0xFFFFFFFF.toInt()
    private val globalStack = MatrixStack()

    fun stack(): MatrixStack = globalStack

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
        layer: RenderLayer = QuadLayer
    ) {
        val stack = globalStack.peek() ?: return
        val bufr = getBuffer(DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        if (!solid) {
            val x1 = x.toFloat()
            val y1 = y.toFloat()
            val x2 = (x + width).toFloat()
            val y2 = (y + height).toFloat()
            // Top
            drawLine(bufr, stack, x1, y1, x2, y1, lineWidth, color)
            // Left
            drawLine(bufr, stack, x1, y1, x1, y2, lineWidth, color)
            // Right
            drawLine(bufr, stack, x2, y1, x2, y2, lineWidth, color)
            // Bottom
            drawLine(bufr, stack, x1, y2, x2, y2, lineWidth, color)

            val end = bufr.endNullable() ?: return
            layer.draw(end)

            return
        }

        bufr.vertex(stack, x.toFloat(), (y + height).toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack, (x + width).toFloat(), (y + height).toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack, (x + width).toFloat(), y.toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack, x.toFloat(), y.toFloat(), 0f).color(color.rgb)

        val end = bufr.endNullable() ?: return
        layer.draw(end)
    }

    @JvmOverloads
    fun drawInvertedColRect(x: Double, y: Double, width: Double, height: Double, alpha: Float = 255f) {
        drawRect(x, y, width, height, color = Color.BLUE.withAlpha(alpha), layer = TextHighlightLayer)
    }

    fun drawColorGradient(
        x: Double, y: Double, width: Double, height: Double,
        color: Color = Color.WHITE
    ) {
        val stack = globalStack.peek() ?: return
        val bufr = getBuffer(DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        bufr.vertex(stack, x.toFloat(), (y + height).toFloat(), 0f).color(Color.BLACK.rgb)
        bufr.vertex(stack, (x + width).toFloat(), (y + height).toFloat(), 0f).color(Color.BLACK.rgb)
        bufr.vertex(stack, (x + width).toFloat(), y.toFloat(), 0f).color(color.rgb)
        bufr.vertex(stack, x.toFloat(), y.toFloat(), 0f).color(Color.WHITE.rgb)

        val end = bufr.endNullable() ?: return
        QuadLayer.draw(end)
    }

    fun Color.withAlpha(alpha: Float): Color {
        return Color(
            this.red.toFloat() / 255f,
            this.green.toFloat() / 255f,
            this.blue.toFloat() / 255f,
            (alpha / 255f).coerceIn(0f, 1f)
        )
    }

    fun getMouseX(sr: ScaledResolution): Double =
        sr.mc.mouse.x * sr.mc.window.scaledWidth / max(1, sr.mc.window.width)

    fun getMouseY(sr: ScaledResolution): Double =
        sr.mc.mouse.y * sr.mc.window.scaledHeight / max(1, sr.mc.window.height)

    @JvmOverloads
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false, color: Int = WHITE) {
        val stack = globalStack.peek() ?: return
        var _y = y
        val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
        text.split('\n').forEach {
            fontRenderer.draw(
                it,
                x,
                _y,
                color,
                shadow,
                stack.positionMatrix,
                immediate,
                TextRenderer.TextLayerType.NORMAL,
                0,
                0xf000f0,
            )
            _y += fontRenderer.fontHeight
        }
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
