package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIClickEvent(
    x: Double,
    y: Double,
    var button: Int,
    component: UIElement
) : UIMouseEvent(x, y, component)