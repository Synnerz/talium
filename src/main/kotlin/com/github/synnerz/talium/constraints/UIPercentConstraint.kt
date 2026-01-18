package com.github.synnerz.talium.constraints

import com.github.synnerz.talium.components.UIElement

class UIPercentConstraint(override var value: Double, override var parent: UIElement) : UIConstraint {
    override fun onUpdate() {
        // TODO: add caching and update here the cache
    }

    override fun x(): Double {
        return value / 100 * parent.width + parent.x
    }

    override fun x(parent: UIElement): Double {
        return value / 100 * parent.width + parent.x
    }

    override fun y(): Double {
        return value / 100 * parent.height + parent.y
    }

    override fun y(parent: UIElement): Double {
        return value / 100 * parent.height + parent.y
    }

    override fun width(): Double {
        return value / 100 * parent.width
    }

    override fun width(parent: UIElement): Double {
        return value / 100 * parent.width
    }

    override fun height(): Double {
        return value / 100 * parent.height
    }

    override fun height(parent: UIElement): Double {
        return value / 100 * parent.height
    }
}