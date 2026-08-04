package com.github.synnerz.talium.utils

import com.github.synnerz.talium.mixin.accessor.GameRendererAccessor
import com.github.synnerz.talium.mixin.accessor.GuiRendererAccessor
import com.github.synnerz.talium.utils.state.GradientRectangleState
import com.github.synnerz.talium.utils.state.SimpleLineState
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.client.renderer.state.gui.GuiTextRenderState
import net.minecraft.network.chat.Component
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fStack
import java.awt.Color
import kotlin.math.max

object Renderer {
    private val guiRenderState: GuiRenderState by lazy {
        ((Minecraft.getInstance().gameRenderer as GameRendererAccessor).guiRenderer as GuiRendererAccessor).renderState
    }
    val fontRenderer: Font by lazy { Minecraft.getInstance().font }
    private const val WHITE: Int = 0xFFFFFFFF.toInt()
    var stack = Matrix3x2fStack(128)
    val scissorStack = ScissorStack()

    fun stack() = stack

    fun submit(state: GuiElementRenderState) {
        guiRenderState.addGuiElement(state)
    }

    fun submitRect(
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        colorStart: Color,
        colorEnd: Color = colorStart
    ) {
        guiRenderState.addGuiElement(
            GradientRectangleState(
                Matrix3x2f(stack),
                x1, y1, x2, y2,
                colorStart.rgb, colorStart.rgb,
                colorEnd.rgb, colorEnd.rgb,
                scissorArea = scissorStack.peek()
            )
        )
    }

    fun submitInvertedRect(
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        color: Color,
    ) {
        guiRenderState.addGuiElement(
            GradientRectangleState(
                Matrix3x2f(stack),
                x1, y1, x2, y2,
                color.rgb, color.rgb,
                color.rgb, color.rgb,
                RenderPipelines.GUI_INVERT,
                scissorArea = scissorStack.peek()
            )
        )
    }

    fun submitGradientRect(
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        color: Color,
    ) {
        guiRenderState.addGuiElement(
            GradientRectangleState(
                Matrix3x2f(stack),
                x1, y1,
                x2, y2,
                Color.WHITE.rgb, color.rgb,
                Color.BLACK.rgb, Color.BLACK.rgb,
                scissorArea = scissorStack.peek()
            )
        )
    }

    fun submitLine(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        thickness: Float = 1f,
        color: Color
    ) {
        guiRenderState.addGuiElement(
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
            GuiTextRenderState(
                fontRenderer, Component.literal(text).visualOrderText, Matrix3x2f(stack),
                x, y, color, 0, shadow,
                false, // surely this is right ?
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

        submitRect(x, y, x + width, y + height, color)
    }

    @JvmOverloads
    fun drawInvertedColRect(x: Double, y: Double, width: Double, height: Double, alpha: Float = 255f) {
        submitInvertedRect(x, y, x + width, y + height, Color.BLUE.withAlpha(alpha))
    }

    fun drawColorGradient(
        x: Double, y: Double, width: Double, height: Double,
        color: Color = Color.WHITE
    ) {
        submitGradientRect(
            x, y, x + width, y + height,
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
        sr.mc.mouseHandler.getScaledXPos(sr.mc.window)

    fun getMouseY(sr: ScaledResolution): Double =
        sr.mc.mouseHandler.getScaledYPos(sr.mc.window)

    @JvmOverloads
    fun drawString(text: String, x: Float, y: Float, shadow: Boolean = false, color: Int = WHITE) {
        var _y = y
        text.split('\n').forEach {
            submitText(it, x.toInt(), _y.toInt(), color, shadow)
            _y += fontRenderer.lineHeight
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

    fun String.trimToWidth(width: Double): String = fontRenderer.plainSubstrByWidth(this, width.toInt())

    fun String.getWidth() = fontRenderer.width(ChatFormatting.stripFormatting(this)!!)

    class ScissorStack(val _stack: MutableList<ScreenRectangle> = mutableListOf()) {
        fun push(x: Int, y: Int, width: Int, height: Int) {
            _stack.add(ScreenRectangle(x, y, width, height))
        }

        fun peek(): ScreenRectangle? {
            return _stack.lastOrNull()
        }

        fun pop() {
            _stack.removeLast()
        }
    }
}
