package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.events.*
import net.minecraft.client.renderer.GlStateManager

open class UIScrollable @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var visibleComponents: MutableList<UIBase> = mutableListOf()
    var miny = 0.0

    init {
        addEffect(ScissorEffect())
    }

    override fun onUpdate() = apply {
        getScrollY(12)
        visibleComponents = getVisibleComponents(miny, miny + height)
    }

    open fun getScrollY(yOffset: Int): Double {
        val comp = children.last()
        if (comp.isDirty()) comp.update()

        miny -= yOffset
        val maxScroll = (comp.bounds.y2 - height + 5.0).coerceAtLeast(y)
        miny = miny.coerceIn(y, maxScroll)

        return miny
    }

    open fun getVisibleComponents(scrollY: Double, maxHeight: Double): MutableList<UIBase> {
        val comps = mutableListOf<UIBase>()

        for (child in children.toList()) {
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
    }

    override fun onMouseScroll(event: UIScrollEvent) = apply {
        if (children.isEmpty()) return@apply

        val d = event.delta * 120
        val yOffset = d / 10
        getScrollY(yOffset)

        visibleComponents = getVisibleComponents(miny, miny + height)
    }

    override fun propagateMouseScroll(event: UIScrollEvent) {
        onMouseScroll(event)
        hookMouseScroll?.invoke(event)
        if (!event.propagate) return

        val resizedEvent = UIScrollEvent(event.x, event.y + (miny - y), event.delta, event.component)
        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseScroll(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseClick(event: UIClickEvent) {
        onMouseClick(event)
        hookMouseClick?.invoke(event)
        if (!event.propagate) return

        val resizedEvent = UIClickEvent(event.x, event.y + (miny - y), event.button, event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseClick(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseRelease(event: UIClickEvent) {
        onMouseRelease(event)
        hookMouseRelease?.invoke(event)
        if (!event.propagate) return

        val resizedEvent = UIClickEvent(event.x, event.y + (miny - y), event.button, event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseRelease(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseEnter(event: UIMouseEvent) {
        if (!mouseInBounds) {
            onMouseEnter(event)
            hookMouseEnter?.invoke(event)
            mouseInBounds = true
        }
        if (!event.propagate) return

        val resizedEvent = UIMouseEvent(event.x, event.y + (miny - y), event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseEnter(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseLeave(event: UIMouseEvent) {
        if (mouseInBounds && !inBounds(event)) {
            onMouseLeave(event)
            hookMouseLeave?.invoke(event)
            mouseInBounds = false
        }
        if (!event.propagate) return

        val resizedEvent = UIMouseEvent(event.x, event.y + (miny - y), event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (child.hidden) continue
            child.propagateMouseLeave(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseHover(event: UIMouseEvent) {
        onMouseHover(event)
        hookMouseHover?.invoke(event)
        if (!event.propagate) return

        val resizedEvent = UIMouseEvent(event.x, event.y + (miny - y), event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseHover(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateMouseDrag(event: UIDragEvent) {
        onMouseDrag(event)
        hookMouseDrag?.invoke(event)
        if (!event.propagate) return

        val resizedEvent = UIDragEvent(event.dx, event.dy + (miny - y), event.x, event.y + (miny - y), event.button, event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            child.onMouseDragOut(resizedEvent)
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateMouseDrag(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateFocus(event: UIFocusEvent) {
        if (focused != event.state) {
            focused = true
            onFocus(event)
            hookFocus?.invoke(event)
            focused = event.state
        }
        if (!event.propagate) return

        val resizedEvent = UIFocusEvent(event.x, event.y + (miny - y), event.state, event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (!child.inBounds(resizedEvent) || child.hidden) continue

            child.propagateFocus(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }

    override fun propagateUnfocus(event: UIFocusEvent) {
        if (focused && !inBounds(event)) {
            focused = false
            onUnfocus(event)
            onLostFocus(event)
            hookUnfocus?.invoke(event)
        }
        if (!event.propagate) return

        val resizedEvent = UIFocusEvent(event.x, event.y + (miny - y), event.state, event.component)

        onPreChildPropagate(resizedEvent)
        preChildPropagate?.let { it(resizedEvent) }

        for (child in children.toList()) {
            if (child.hidden) continue
            child.propagateUnfocus(resizedEvent)
            if (!resizedEvent.propagate) break
        }

        onPostChildPropagation(resizedEvent)
        postChildPropagate?.let { it(resizedEvent) }
    }
}
