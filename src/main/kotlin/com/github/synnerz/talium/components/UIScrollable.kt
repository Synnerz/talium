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
    var visibleComponents: MutableList<UIBase> = mutableListOf()
    var miny = 0.0
    var shouldDisplayScroll = false

    init {
        addEffect(ScissorEffect())
    }

    override fun onUpdate() = apply {
        getScrollY(12)
        visibleComponents = getVisibleComponents(miny, miny + height)
        shouldDisplayScroll = visibleComponents.size < children.size
    }

    open fun getScrollY(yOffset: Int): Double {
        val comp = children.last()
        if (comp.isDirty()) comp.update()

        miny = max(y, miny - yOffset)
        miny = if (comp.bounds.y2 > height) min(miny, comp.bounds.y2 - height + 5.0)
               else min(miny, miny - 12)

        return miny
    }

    open fun getVisibleComponents(scrollY: Double, maxHeight: Double): MutableList<UIBase> {
        val comps = mutableListOf<UIBase>()

        for (child in children) {
            if (child.hidden) continue
            if (child.isDirty()) child.update()
            if (child.bounds.y1 < scrollY || child.bounds.y2 > maxHeight) continue
            comps.add(child)
        }

        return comps
    }

    override fun drawChildren(x2: Double, y2: Double) {
        for (child in visibleComponents) {
            child.draw(0.0, miny)
        }
        GlStateManager.popMatrix()
    }

    override fun render() {
        UIRect.drawRect(x, y, width, height)

        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, y, 0.0)

        if (shouldDisplayScroll) {
            // TODO: fix sliderbar sometimes offsetting to middle rather than properly going down
            val lastHeight = children.lastOrNull()?.bounds?.y2 ?: height
            val scroll = lastHeight - height
            val barHeight = (height / lastHeight) * height
            val barY = (miny - y) / (scroll - y) * (height - barHeight)
            UIRect.drawRect(x + width - 5.0, barY, 5.0, barHeight)
        }
    }

    override fun onMouseScroll(event: UIScrollEvent) = apply {
        val d = event.delta * 120
        val yOffset = d / 10
        getScrollY(yOffset)

        visibleComponents = getVisibleComponents(miny, miny + height)
    }

    override fun propagateMouseClick(event: UIClickEvent) {
        val newEvent = UIClickEvent(event.x, event.y + (miny - y), event.button, event.component)
        onMouseClick(newEvent)
        hookOnMouseClick(newEvent)
        if (!newEvent.propagate) return

        for (child in visibleComponents) {
            if (!child.inBounds(newEvent)) continue

            child.propagateMouseClick(newEvent)
            if (!newEvent.propagate) break
        }
    }
}
