package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIBase

open class UIFocusEvent(
    val focused: Boolean,
    val component: UIBase
) : UIEvent()