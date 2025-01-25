package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIDragEvent(
    val dx: Double,
    val dy: Double,
    x: Double,
    y: Double,
    val button: Int,
    component: UIBase
) : UIMouseEvent(x, y, component)