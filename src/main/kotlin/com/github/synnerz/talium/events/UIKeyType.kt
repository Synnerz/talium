package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIKeyType(
    val keycode: Int,
    val char: Char,
    val component: UIBase
) : UIEvent()