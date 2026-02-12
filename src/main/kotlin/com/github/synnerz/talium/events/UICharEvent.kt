package com.github.synnerz.talium.events

import com.github.synnerz.talium.components.UIElement

open class UICharEvent(
    val codepoint: Int,
    val str: String,
    val modifiers: Int,
    var component: UIElement,
) : UIEvent() {
    fun hasAlt(): Boolean = (modifiers and 4) != 0
    fun hasShift(): Boolean = (modifiers and 1) != 0
    fun hasCtrl(): Boolean = (modifiers and 2) != 0
    fun hasControl(): Boolean = hasCtrl()
}