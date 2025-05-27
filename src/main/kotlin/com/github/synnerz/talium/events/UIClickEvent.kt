package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIClickEvent(
    x: Double,
    y: Double,
    var button: Int,
    component: UIBase
) : UIMouseEvent(x, y, component)