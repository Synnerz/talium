package com.github.synnerz.talium.effects

import com.github.synnerz.talium.components.UIElement

open class UIEffect {
    var component: UIElement? = null
    /**
     * * Whether this effect adds its own color to the stack or not
     * * i.e. if i want this effect to color the component red i'd set this to `true`
     */
    open var forceColor: Boolean = false
    /**
     * * Override this method if you need to do something **before** the component is drawn
     */
    open fun preDraw(x2: Double = 0.0, y2: Double = 0.0) {}
    /**
     * * Override this method if you need to do something **after** the component is drawn
     */
    open fun postDraw() {}
    /**
     * * Override this method if you need to do something **before** the children are drawn
     */
    open fun preChildDraw() {}
    /**
     * * Override this method if you need to do something **after** the children are drawn
     */
    open fun postChildDraw() {}
}