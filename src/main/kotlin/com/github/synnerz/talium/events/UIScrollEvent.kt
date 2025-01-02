package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIScrollEvent(
    val x: Double,
    val y: Double,
    val delta: Int,
    val component: UIBase
) : UIEvent()