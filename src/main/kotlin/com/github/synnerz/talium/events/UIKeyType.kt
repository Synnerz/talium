package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIKeyType(
    var keycode: Int,
    var char: Char?,
    var string: String,
    var component: UIElement
) : UIEvent()