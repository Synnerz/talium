package com.github.synnerz.talium.components

open class UIDecimalSlider @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    value: Double = 0.0,
    min: Double,
    max: Double,
    radius: Double = 0.0,
    parent: UIElement? = null
) : UISlider(_x, _y, _width, _height, value, min, max, radius, parent) {
    override fun getDisplayValue(): String = String.format("%.2f", getCurrentValue())

    override fun validateString(str: String): Boolean = str.all { it.isDigit() || it == '.' }
}