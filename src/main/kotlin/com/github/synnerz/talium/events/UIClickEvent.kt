package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIClickEvent(
    x: Double,
    y: Double,
    val button: Int,
    component: UIBase
) : UIMouseEvent(x, y, component)