package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIFocusEvent(
    x: Double,
    y: Double,
    val state: Boolean,
    component: UIBase
) : UIMouseEvent(x, y, component)