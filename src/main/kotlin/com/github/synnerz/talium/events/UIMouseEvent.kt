package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIMouseEvent(
    var x: Double,
    var y: Double,
    var component: UIElement
) : UIEvent()