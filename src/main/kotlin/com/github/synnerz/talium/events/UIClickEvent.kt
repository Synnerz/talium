package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIClickEvent(
    val x: Double,
    val y: Double,
    val button: Int,
    val component: UIBase
) : UIEvent()