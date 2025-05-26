package com.github.synnerz.talium.components

import kotlin.math.roundToInt

open class UIPercentSlider @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    value: Double = 0.0,
    min: Double,
    max: Double,
    radius: Double = 0.0,
    parent: UIBase? = null
) : UIDecimalSlider(_x, _y, _width, _height, value, min, max, radius, parent) {
    override fun getDisplayValue(): String = "${value.roundToInt()}%"
}