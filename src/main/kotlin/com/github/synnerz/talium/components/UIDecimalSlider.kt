package com.github.synnerz.talium.components

import kotlin.math.roundToInt

open class UIDecimalSlider @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    value: Double = 0.0,
    min: Double,
    max: Double,
    radius: Double = 0.0,
    parent: UIBase? = null
) : UISlider(_x, _y, _width, _height, value * 100, min * 100, max * 100, radius, parent) {
    override fun getCurrentValue(): Double = value.roundToInt() / 100.0

    override fun getDisplayValue(): String = String.format("%.1f", getCurrentValue())
}