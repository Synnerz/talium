package com.github.synnerz.talium.layout

import com.github.synnerz.talium.components.UIElement

open class Layout {
    var parent: UIElement? = null

    open fun onUpdate() {}
    open fun preChildDraw() {}
    open fun postChildDraw() {}
}