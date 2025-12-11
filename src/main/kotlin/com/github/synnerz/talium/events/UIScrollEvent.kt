package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIScrollEvent(
    x: Double,
    y: Double,
    var delta: Int,
    component: UIElement
) : UIMouseEvent(x, y, component)