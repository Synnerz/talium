package com.github.synnerz.talium.constraints

import com.github.synnerz.talium.components.UIElement
import kotlin.math.max
import kotlin.math.min

class UIFitChildConstraint : UIConstraint {
    override var value: Double = 0.0
        set(_) {}

    override fun onUpdate(parent: UIElement) {}

    override fun x(parent: UIElement): Double {
        val children = parent.children
        if (children.isEmpty()) return 0.0

        var x = children[0].bounds.x1
        for (i in 1 until children.size) {
            x = min(x, children[i].bounds.x1)
        }

        return x
    }

    override fun y(parent: UIElement): Double {
        val children = parent.children
        if (children.isEmpty()) return 0.0

        var y = children[0].bounds.y1
        for (i in 1 until children.size) {
            y = min(y, children[i].bounds.y1)
        }

        return y
    }

    override fun width(parent: UIElement): Double {
        val children = parent.children
        if (children.isEmpty()) return 0.0

        var x1 = children[0].bounds.x1
        var x2 = children[0].bounds.x2
        for (i in 1 until children.size) {
            x1 = min(x1, children[i].bounds.x1)
            x2 = max(x2, children[i].bounds.x2)
        }

        return x2 - x1
    }

    override fun height(parent: UIElement): Double {
        val children = parent.children
        if (children.isEmpty()) return 0.0

        var y1 = children[0].bounds.y1
        var y2 = children[0].bounds.y2
        for (i in 1 until children.size) {
            y1 = min(y1, children[i].bounds.y1)
            y2 = max(y2, children[i].bounds.y2)
        }

        return y2 - y1
    }
}