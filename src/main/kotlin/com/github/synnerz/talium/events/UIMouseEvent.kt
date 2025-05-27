package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIMouseEvent(
    var x: Double,
    var y: Double,
    var component: UIBase
) : UIEvent()