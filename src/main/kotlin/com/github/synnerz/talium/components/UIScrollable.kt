package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIScrollEvent
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.max
import kotlin.math.min

open class UIScrollable @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var drawChildren: Boolean = false
    var totalHeight = 0.0
    var totalVisibleHeight = 0.0
    lateinit var visibleComponents: MutableList<UIBase>
    var miny = 0.0

    init {
        addEffect(ScissorEffect())
    }

    open fun getVisibleComponents(scrollY: Double, maxHeight: Double): MutableList<UIBase> {
        val comps = mutableListOf<UIBase>()
        totalHeight = 0.0
        totalVisibleHeight = 0.0

        for (child in children) {
            if (child.x == 0.0) child.update()
            totalHeight += child.height
            if (child.bounds.y1 >= scrollY && child.bounds.y2 <= maxHeight) {
                totalVisibleHeight += child.height
                comps.add(child)
            }
        }

        return comps
    }

    override fun render() {
        if (!::visibleComponents.isInitialized)
            visibleComponents = getVisibleComponents(y, y + height)

        UIRect.drawRect(x, y, width, height)

        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, y, 0.0)

        for (child in visibleComponents) {
            child.draw(0.0, miny)
        }

        GlStateManager.popMatrix()
    }

    override fun onMouseScroll(event: UIScrollEvent) = apply {
        val d = event.delta * 120
        val yOffset = d / 10
        val comp = children.last()

        miny = max(y, miny - yOffset)
        miny = min(miny, comp.bounds.y2 - height + 5.0)
        visibleComponents = getVisibleComponents(miny, miny + height)
    }

    override fun propagateMouseClick(event: UIClickEvent) {
        val newEvent = UIClickEvent(event.x, event.y + (miny - y), event.button, event.component)
        onMouseClick(newEvent)
        hookOnMouseClick(newEvent)
        if (!newEvent.propagate) return

        for (child in children) {
            if (!child.inBounds(newEvent)) continue

            child.propagateMouseClick(newEvent)
            if (!newEvent.propagate) break
        }
    }
}
