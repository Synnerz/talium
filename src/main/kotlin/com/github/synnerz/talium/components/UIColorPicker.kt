package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.OutlineEffect
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIDragEvent
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.state.GradientRectangleState
import org.joml.Matrix3x2f
import java.awt.Color
import kotlin.math.max
import kotlin.math.roundToInt

open class UIColorPicker @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Int = -1, // argb
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var alpha = value ushr 24
    private val defaultColor = Color(value, true)
    private val hsbColor = Color.RGBtoHSB(defaultColor.red, defaultColor.green, defaultColor.blue, null)

    val floatingChild = object : FloatingUI(this) {
        override fun setFloatingPos(parent: UIElement) {
            x = parent.x
            y = parent.y + parent.height * 1.01
            val dim = max(parent.width, parent.height)
            width = dim * 1.03
            height = dim * 1.05
        }
    }.also { it.hide() }
    val floatingBg = UIRect(0.0, 0.0, 100.0, 100.0, parent = floatingChild)
    open val alphaSlider = object : UIDecimalSlider(2.0, 84.0, 96.0, 12.0, alpha / 255.0, 0.0, 1.0, parent = floatingBg) {
        override fun setCurrentX(x: Double) {
            super.setCurrentX(x)
            setAlpha(getCurrentValue())
        }

        override fun setCurrentValue(value: Double) {
            super.setCurrentValue(value)
            setAlpha(getCurrentValue())
        }
    }.apply { hide() }
    override var bgColor: Color = Color(0, 0, 0, 0)
        set(value) {
            alphaSlider.setColor(value.brighter())
            floatingBg.setColor(value)
            field = value
        }
    open val huePicker = UIColorHuePicker(2.0, 2.0, 15.0, 80.0, hsbColor[0].toDouble(), parent = floatingBg).apply {
        colorPicker = this@UIColorPicker
        hide()
    }
    open val gradientPicker = UIColorGradient(18.0, 2.0, 80.0, 80.0, Color(Color.HSBtoRGB(hsbColor[0], 1f, 1f)), hsbColor[1].toDouble(), hsbColor[2].toDouble(), parent = floatingBg).apply {
        colorPicker = this@UIColorPicker
        hide()
    }
    open val colorRect = UIRect(4.0, 10.0, 70.0, 80.0, parent = this).apply { bgColor = Color(value, true) }
    var arrowToggle = false
    open val arrowRect = UIRect(85.0, 2.0, 15.0, 100.0, parent = this)
    open val arrowText = UIText(0.0, 0.0, 100.0, 100.0, "↓", true, arrowRect).apply {
        textScale = 2f
    }

    override fun render() {
        Renderer.drawRect(x, y, width, height, color = bgColor)
    }

    open fun setValue(hue: Double) {
        gradientPicker.setValue(hue)
        val newColor = Color(
            Color.HSBtoRGB(
                hue.toFloat(),
                gradientPicker.saturation.toFloat(),
                gradientPicker.brightness.toFloat()
            )
        )
        colorRect.bgColor = Color(
            newColor.red,
            newColor.green,
            newColor.blue,
            alpha
        )
        value = colorRect.bgColor.rgb
    }

    open fun setRgb(argb: Int) {
        val ncolor = Color(argb, true)
        val hsb = Color.RGBtoHSB(ncolor.red, ncolor.green, ncolor.blue, null)
        huePicker.setHue(hsb[0].toDouble())
        gradientPicker.setValue(ncolor, 1.0, 1.0)
        alpha = ncolor.alpha
        colorRect.bgColor = ncolor
        value = colorRect.bgColor.rgb
    }

    open fun setAlpha(alpha: Double) {
        this.alpha = (alpha * 255).roundToInt()
        setValue(huePicker.currentHue)
    }

    open fun onSatBriChange() {
        setValue(huePicker.currentHue)
    }

    open fun hideDropdown() {
        floatingChild.hide()
        gradientPicker.hide()
        huePicker.hide()
        alphaSlider.hide()
        arrowText.text = "↓"
        arrowToggle = false
    }

    open fun unhideDropdown() {
        floatingChild.unhide()
        gradientPicker.unhide()
        huePicker.unhide()
        alphaSlider.unhide()
        arrowText.text = "↑"
        arrowToggle = true
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply

        if (!arrowToggle) {
            unhideDropdown()
            return@apply
        }

        hideDropdown()
    }
}

open class UIColorHuePicker @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var currentHue: Double = 1.0,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    private val hueColors = List(51) {
        Color(Color.HSBtoRGB(it / 50f, 1f, 0.7f)).rgb
    }
    private val huePointer = UIRect(0.0, currentHue * 100, 100.0, 2.0, parent = this).also {
        it.addEffect(OutlineEffect())
    }
    private var dragging = false
    var colorPicker: UIColorPicker? = null

    override fun render() {
        val N = hueColors.size - 1
        val h = height / N
        for (idx in 0 until N) {
            val color1 = hueColors[idx]
            val color2 = hueColors[idx + 1]
            val yPos = y + idx * h
            Renderer.submit(
                GradientRectangleState(
                    Matrix3x2f(Renderer.stack),
                    x, yPos,
                    x + width, yPos + h,
                    color1, color1,
                    color2, color2,
                    bounds = Renderer.scissorStack.peek()
                )
            )
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply

        val ry = (event.y - y).coerceIn(0.0, height) - 1

        currentHue = (ry / height).coerceIn(0.0, 1.0)
        colorPicker?.setValue(currentHue)
        // Set pointer to the current hue position
        huePointer._y = currentHue * 100
        huePointer.setDirty()
        dragging = true
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!dragging || event.button != 0) return@apply

        val ry = (event.y - y).coerceIn(0.0, height) - 1

        currentHue = (ry / height).coerceIn(0.0, 1.0)
        colorPicker?.setValue(currentHue)

        // Set pointer to the current hue position
        huePointer._y = currentHue * 100
        huePointer.setDirty()
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply
        huePointer._y = currentHue * 100
        huePointer.setDirty()
        dragging = false
    }

    fun setHue(newHue: Double) {
        currentHue = newHue
        huePointer._y = currentHue * 100
        huePointer.setDirty()
    }
}

open class UIColorGradient @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var color: Color = Color.WHITE,
    var saturation: Double = 1.0,
    var brightness: Double = 1.0,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    private val gradientPointer = UIRect(saturation * 100, (1 - brightness) * 100, 4.0, 4.0, parent = this).also {
        it.addEffect(OutlineEffect())
    }
    private var dragging = false
    var colorPicker: UIColorPicker? = null

    override fun render() {
        Renderer.drawColorGradient(x, y, width, height, color)
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply

        val rx = (event.x - x).coerceIn(0.0, width)
        val ry = (event.y - y).coerceIn(0.0, height)

        saturation = (rx / width).coerceIn(0.0, 0.94)
        brightness = (1 - (ry / height)).coerceIn(0.0, 1.0)
        colorPicker?.onSatBriChange()
        dragging = true

        gradientPointer._x = saturation * 100
        gradientPointer._y = (1 - brightness) * 100
        gradientPointer.setDirty()
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!dragging || event.button != 0) return@apply

        val rx = (event.x - x).coerceIn(0.0, width)
        val ry = (event.y - y).coerceIn(0.0, height)

        saturation = (rx / width).coerceIn(0.0, 0.94)
        brightness = (1 - (ry / height)).coerceIn(0.0, 1.0)
        colorPicker?.onSatBriChange()

        // Set pointer to the current saturation/brightness position
        gradientPointer._x = saturation * 100
        gradientPointer._y = (1 - brightness) * 100
        gradientPointer.setDirty()
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply
        gradientPointer._x = saturation * 100
        gradientPointer._y = (1 - brightness) * 100
        gradientPointer.setDirty()
        dragging = false
    }

    open fun setValue(hue: Double) {
        color = Color(Color.HSBtoRGB(hue.toFloat(), 1f, 1f))
    }

    fun setValue(color: Color, sat1: Double, bri1: Double) {
        this.color = color
        saturation = sat1
        brightness = bri1
        gradientPointer._x = saturation * 100
        gradientPointer._y = (1 - brightness) * 100
        gradientPointer.setDirty()
    }
}