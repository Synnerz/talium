package com.github.synnerz.talium.events

open class UIEvent {
    /**
     * * Stops this [UIEvent] from being propagated to the children if set to false
     * * The component will still process it
     */
    open var propagate: Boolean = true

    /**
     * * Stops this [UIEvent] from being propagated to the children
     * * The component will still process it
     */
    open fun stopPropagation() {
        propagate = false
    }
}