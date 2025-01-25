package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIScrollEvent(
    x: Double,
    y: Double,
    val delta: Int,
    component: UIBase
) : UIMouseEvent(x, y, component)