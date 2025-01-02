package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIMouseEvent(
    val x: Double,
    val y: Double,
    val component: UIBase
) : UIEvent()