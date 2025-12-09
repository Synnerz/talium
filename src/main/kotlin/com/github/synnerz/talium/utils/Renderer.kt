package com.github.synnerz.talium.utils

import com.github.synnerz.talium.mixin.accessor.GameRendererAccessor
import com.github.synnerz.talium.utils.state.ColorCustomQuadState
import com.github.synnerz.talium.utils.state.SimpleLineState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState
import net.minecraft.client.gui.render.state.GuiRenderState
import net.minecraft.client.gui.render.state.TextGuiElementRenderState
import net.minecraft.client.texture.TextureSetup
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fStack
import java.awt.Color
import kotlin.math.max

object Renderer {
    private val guiRenderState: GuiRenderState by lazy {
        (MinecraftClient.getInstance().gameRenderer as GameRendererAccessor).guiState
    }
    val fontRenderer: TextRenderer by lazy { MinecraftClient.getInstance().textRenderer }
    private const val WHITE: Int = 0xFFFFFFFF.toInt()
    var stack = Matrix3x2fStack(128)
    val scissorStack = ScissorStack()

    fun stack() = stack

    fun submitRect(
        x1: Int, y1: Int,
        x2: Int, y2: Int,
        colorStart: Color,
        colorEnd: Color = colorStart
    ) {
        guiRenderState.addSimpleElement(
            ColoredQuadGuiElementRenderState(
                RenderPipelines.GUI, TextureSetup.empty(), Matrix3x2f(stack),
                x1, y1, x2, y2, colorStart.rgb, colorEnd.rgb,
                scissorStack.peek()
            )
        )
    }

    fun submitInvertedRect(
        x1: Int, y1: Int,
        x2: Int, y2: Int,
        color: Color,
    ) {
        guiRenderState.addSimpleElement(
            ColoredQuadGuiElementRenderState(
                RenderPipelines.GUI_INVERT, TextureSetup.empty(), Matrix3x2f(stack),
                x1, y1, x2, y2, color.rgb, color.rgb,
                scissorStack.peek()
            )
        )
    }

    fun submitGradientRect(
        x1: Int, y1: Int,
        x2: Int, y2: Int,
        color: Color,
    ) {
        guiRenderState.addSimpleElement(
            ColorCustomQuadState(
                Matrix3x2f(stack),
                x1, y1, x2, y2, color,
                scissorStack.peek()
            )
        )
    }

    fun submitLine(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        thickness: Float = 1f,
        color: Color
    ) {
        guiRenderState.addSimpleElement(
            SimpleLineState(
                Matrix3x2f(stack),
                x1, y1, x2, y2, thickness, color,
                scissorStack.peek()
            )
        )
    }

    fun submitText(
        text: String,
        x: Int, y: Int,
        color: Int = -1,
        shadow: Boolean = true
    ) {
        guiRenderState.addText(
            TextGuiElementRenderState(
                fontRenderer, Text.literal(text).asOrderedText(), Matrix3x2f(stack),
                x, y, color, 0, shadow,
                scissorStack.peek()
            )
        )
    }

    @JvmOverloads
    fun drawRect(
        x: Double, y: Double, width: Double, height: Double,
        solid: Boolean = true,
        color: Color = Color.WHITE,
        lineWidth: Float = 1f
    ) {
        if (!solid) {
            val x1 = x.toFloat()
            val y1 = y.toFloat()
            val x2 = (x + width).toFloat()
            val y2 = (y + height).toFloat()
            submitLine(x1, y1, x2, y1, lineWidth, color)
            submitLine(x1, y1, x1, y2, lineWidth, color)
            submitLine(x2, y1, x2, y2, lineWidth, color)
            submitLine(x1, y2, x2, y2, lineWidth, color)

            return
        }

        submitRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color)
    }

    @JvmOverloads
    fun drawInvertedColRect(x: Double, y: Double, width: Double, height: Double, alpha: Float = 255f) {
        submitInvertedRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), Color.BLUE.withAlpha(alpha))
    }

    fun drawColorGradient(
        x: Double, y: Double, width: Double, height: Double,
        color: Color = Color.WHITE
    ) {
        submitGradientRect(
            x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(),
            color
        )
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
        var _y = y
        text.split('\n').forEach {
            submitText(it, x.toInt(), _y.toInt(), color, shadow)
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

    class ScissorStack(val _stack: MutableList<ScreenRect> = mutableListOf()) {
        fun push(x: Int, y: Int, width: Int, height: Int) {
            _stack.add(ScreenRect(x, y, width, height))
        }

        fun peek(): ScreenRect? {
            return _stack.lastOrNull()
        }

        fun pop() {
            _stack.removeLast()
        }
    }
}
