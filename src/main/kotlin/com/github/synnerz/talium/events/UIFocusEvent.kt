package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIFocusEvent(
    var state: Boolean,
    val component: UIElement,
    val cause: UIMouseEvent?,
) : UIEvent()