package com.github.synnerz.talium.constraints

import com.github.synnerz.talium.components.UIElement

class UIFlexWrapConstraint(
    override var value: Double,
) : UIPositionConstraint {
    override fun x(parent: UIElement): Double {
        val idx = parent.parent!!.children.indexOf(parent)
        if (idx == -1 || idx == 0) return parent.parent?.x ?: 0.0
        var sibling = parent.parent?.children?.getOrNull(idx - 1) ?: return 0.0
        if (sibling.hidden || sibling.isDirty())
            sibling = findPreviousChild(idx - 1, parent) ?: return 0.0

        if (sibling.bounds.x2 + parent.width + value - parent.parent!!.bounds.x2 <= 0.2) {
            return sibling.bounds.x2 + value
        }

        return sibling.bounds.x1
    }

    override fun y(parent: UIElement): Double {
        val idx = parent.parent!!.children.indexOf(parent)
        if (idx == -1 || idx == 0) return parent.parent?.y ?: 0.0
        var sibling = parent.parent?.children?.getOrNull(idx - 1) ?: return 0.0
        if (sibling.hidden || sibling.isDirty())
            sibling = findPreviousChild(idx - 1, parent) ?: return 0.0

        return sibling.bounds.y2 + value
    }

    private fun findPreviousChild(idx: Int, parent: UIElement): UIElement? {
        var result: UIElement? = null

        for (jdx in idx - 1 downTo 0) {
            val child = parent.parent?.children?.getOrNull(jdx) ?: continue
            if (!child.hidden && !child.isDirty()) {
                result = child
                break
            }
        }

        return result
    }

    override fun onUpdate(parent: UIElement) {}
}