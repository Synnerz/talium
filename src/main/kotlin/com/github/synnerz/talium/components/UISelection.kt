package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UIClickEvent

open class UISelection @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Int = 0,
    val options: List<String> = listOf(),
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open val centerText = UIText(0.0, 0.0, 100.0, 100.0, options[value], true, this)

    open var rightArrow = object : UISelectionArrow(85.0, 0.0, 15.0, 100.0, ">", this) {
        override fun onMouseRelease(event: UIClickEvent) = apply {
            if (event.button != 0) return@apply
            setOption(value + 1)
        }
    }
        set(value) {
            if (!value.hasParent()) value.setChildOf(this)
            value.onMouseRelease {
                if (it.button != 0) return@onMouseRelease
                setOption(this.value + 1)
            }
            field = value
        }
    open var leftArrow = object : UISelectionArrow(0.0, 0.0, 15.0, 100.0, "<", this) {
        override fun onMouseRelease(event: UIClickEvent) = apply {
            if (event.button != 0) return@apply
            setOption(value - 1)
        }
    }
        set(value) {
            if (!value.hasParent()) value.setChildOf(this)
            value.onMouseRelease {
                if (it.button != 0) return@onMouseRelease
                setOption(this.value - 1)
            }
            field = value
        }

    override fun render() {
        UIRect.drawRect(x, y, width, height, radius, bgColor)
    }

    open fun setOption(idx: Int) {
        value = idx.coerceIn(0, options.lastIndex)
        centerText.text = options[value]
    }
}

open class UISelectionArrow(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    val text: String,
    parent: UISelection? = null
) : UIBase(_x, _y, _width, _height, parent) {
    open val arrowText = UIText(0.0, 0.0, 100.0, 100.0, text, true, this)
}