package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer.trimToWidth

open class UIPasswordInput @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    radius: Double = 0.0,
    text: String = "",
    var secretChar: String = "*",
    parent: UIBase? = null
) : UITextInput(_x, _y, _width, _height, radius, text, parent) {
    // TODO: add a way to toggle this to view/peak the text
    open var protected: Boolean = true

    override fun getCurrentText(): String =
        if (protected) secretChar.repeat(text.substring(currentOffset).length).trimToWidth(width - 8.0, textScale)
        else text.substring(currentOffset).trimToWidth(width - 8.0, textScale)
}