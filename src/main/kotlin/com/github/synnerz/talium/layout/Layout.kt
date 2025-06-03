package com.github.synnerz.talium.layout

import com.github.synnerz.talium.components.UIBase

open class Layout {
    var parent: UIBase? = null

    open fun onUpdate() {}
    open fun preChildDraw() {}
    open fun postChildDraw() {}
}