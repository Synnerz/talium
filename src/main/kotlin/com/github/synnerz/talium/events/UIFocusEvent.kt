package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UIFocusEvent private constructor(
    val cause: UIMouseEvent?,
    val forceState: Boolean?,
    val component: UIElement,
) : UIEvent() {
    constructor(cause: UIMouseEvent, component: UIElement) : this(cause, null, component)
    constructor(forceState: Boolean, component: UIElement) : this(null, forceState, component)
}