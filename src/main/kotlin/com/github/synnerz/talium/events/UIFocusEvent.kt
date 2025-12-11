package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIFocusEvent(
    x: Double,
    y: Double,
    var state: Boolean,
    component: UIElement
) : UIMouseEvent(x, y, component)