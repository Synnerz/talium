package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UICharEvent(
    val codepoint: Int,
    val str: String,
    var component: UIElement,
) : UIEvent()