package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.OutlineEffect
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIDragEvent
import com.github.synnerz.talium.utils.Renderer
import java.awt.Color
import kotlin.math.roundToInt

open class UIColorPicker @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Int = -1, // argb
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var alpha = value ushr 24
    private val defaultColor = Color(value, true)
    private val hsbColor = Color.RGBtoHSB(defaultColor.red, defaultColor.green, defaultColor.blue, null)
    // This is a fake child for a workaround, so we can have a dropdown-like
    // feature for the color picker, this "child" will not have [this] as a parent,
    // so we can branch out and set the width/height to something bigger
    private val fakeChild = UIRect(_x, _y + _height + 1, _width + 3, _height + 5).apply { hide() }
    open val alphaSlider = object : UIDecimalSlider(2.0, 84.0, 96.0, 12.0, 1.0, 0.0, 1.0, parent = fakeChild) {
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
            fakeChild.setColor(value)
            field = value
        }
    open val huePicker = UIColorHuePicker(2.0, 2.0, 15.0, 80.0, hsbColor[0].toDouble(), parent = fakeChild).apply {
        colorPicker = this@UIColorPicker
        hide()
    }
    open val gradientPicker = UIColorGradient(18.0, 2.0, 80.0, 80.0, Color(Color.HSBtoRGB(hsbColor[0], 1f, 1f)), hsbColor[1].toDouble(), hsbColor[2].toDouble(), parent = fakeChild).apply {
        colorPicker = this@UIColorPicker
        hide()
    }
    open val colorRect = UIRect(4.0, 10.0, 70.0, 80.0, parent = this).apply { bgColor = Color(value, true) }
    var arrowToggle = false
    open val arrowRect = UIRect(85.0, 2.0, 15.0, 100.0, parent = this)
    open val arrowText = UIText(0.0, 0.0, 100.0, 100.0, "↓", true, arrowRect).apply {
        textScale = 2f
    }

    override fun draw(x2: Double, y2: Double) {
        super.draw(x2, y2)
        fakeChild.draw()
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

    open fun setAlpha(alpha: Double) {
        this.alpha = (alpha * 255).roundToInt()
        setValue(huePicker.currentHue)
    }

    open fun onSatBriChange() {
        setValue(huePicker.currentHue)
    }

    open fun hideDropdown() {
        fakeChild.hide()
        gradientPicker.hide()
        huePicker.hide()
        alphaSlider.hide()
        arrowText.text = "↓"
        arrowToggle = false
    }

    open fun unhideDropdown() {
        fakeChild.unhide()
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
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    private val hueColors = List(51) {
        Color(Color.HSBtoRGB(it / 50f, 1f, 0.7f))
    }
    private val huePointer = UIRect(0.0, currentHue * 100, 100.0, 2.0, parent = this).also {
        it.addEffect(OutlineEffect())
    }
    private var dragging = false
    var colorPicker: UIColorPicker? = null

    override fun render() {
        for (idx in hueColors.indices) {
            val color = hueColors[idx]
            val yPos = y + ((idx * height) / 50)
            if (yPos + 2.5 > bounds.y2) break
            Renderer.drawRect(x, yPos, width, 2.5, color = color)
        }
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply

        val ry = (event.y - y).coerceIn(0.0, height) - 1

        currentHue = (ry / height).coerceIn(0.0, 1.0)
        colorPicker?.setValue(currentHue)
        // Set pointer to the current hue position
        val percent = currentHue * 100
        huePointer.y = percent / 100 * height + y
        dragging = true
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!dragging || event.button != 0) return@apply

        val ry = (event.y - y).coerceIn(0.0, height) - 1

        currentHue = (ry / height).coerceIn(0.0, 1.0)
        colorPicker?.setValue(currentHue)

        // Set pointer to the current hue position
        val percent = currentHue * 100
        huePointer.y = percent / 100 * height + y
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply
        // On mouse release actually update it, so it doesn't flicker
        // all over the place whenever the user is dragging it
        huePointer._y = currentHue * 100
        huePointer.setDirty()
        dragging = false
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
    parent: UIBase? = null
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
    }

    override fun onMouseDrag(event: UIDragEvent) = apply {
        if (!dragging || event.button != 0) return@apply

        val rx = (event.x - x).coerceIn(0.0, width)
        val ry = (event.y - y).coerceIn(0.0, height)

        saturation = (rx / width).coerceIn(0.0, 0.94)
        brightness = (1 - (ry / height)).coerceIn(0.0, 1.0)
        colorPicker?.onSatBriChange()

        // Set pointer to the current saturation/brightness position
        val percentX = saturation * 100
        val percentY = (1 - brightness) * 100
        gradientPointer.x = percentX / 100 * width + x
        gradientPointer.y = percentY / 100 * height + y
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
}