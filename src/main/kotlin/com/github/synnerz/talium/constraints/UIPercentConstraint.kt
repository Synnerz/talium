package com.github.synnerz.talium.constraints

import com.github.synnerz.talium.components.UIElement

class UIPercentConstraint(override var value: Double) : UIConstraint {
    override fun onUpdate(parent: UIElement) {
        // TODO: add caching and update here the cache
    }

    override fun x(parent: UIElement): Double {
        return value / 100 * parent.width + parent.x
    }

    override fun y(parent: UIElement): Double {
        return value / 100 * parent.height + parent.y
    }

    override fun width(parent: UIElement): Double {
        return value / 100 * parent.width
    }

    override fun height(parent: UIElement): Double {
        return value / 100 * parent.height
    }
}