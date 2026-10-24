package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.events.*
import com.mojang.blaze3d.platform.InputConstants
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

open class UIScrollable @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    /*
    in child coordinates
    min: 0 = top
    max: max child y2 - height = bottom
     */
    var yOffset = 0.0
    var maxY2 = 0.0
    private var visibleComponents = setOf<UIElement>()
    var drawScrollbar = false
    var scrollBarFocused = false
    var scrollBgColor = Color(241, 241, 241, 255)
    var scrollFgColor = Color(136, 136, 136, 255)

    data class ScrollBarBound(
        val x1: Double, val y1: Double, val width1: Double, val height1: Double,
        val x2: Double, val y2: Double, val width2: Double, val height2: Double,
    )

    init {
        addEffect(ScissorEffect())
    }

    override fun onUpdate() = apply {
        children.forEach { it.checkUpdate() }
        updateScrollY()
    }

    fun updateScrollY(dy: Double = 0.0) {
        yOffset += dy

        maxY2 = children.maxOfOrNull { it.bounds.y2 - bounds.y1 }?.also {
            yOffset = yOffset.coerceIn(0.0, max(it - height, 0.0))
        } ?: 0.0

        visibleComponents = children.filter {
            bounds.x1 <= it.bounds.x2 &&
            bounds.y1 <= it.bounds.y2 - yOffset &&
            bounds.x2 >= it.bounds.x1 &&
            bounds.y2 >= it.bounds.y1 - yOffset
        }.toSet()
    }

    fun scrollbarBounds(): ScrollBarBound {
        val w = min(5.0, width * 0.1)
        val h = (height / maxY2) * height
        val yo = (yOffset / maxY2) * height
        val m = w * 0.075

        return ScrollBarBound(
            x + width - w, y, w, height,
            x + width - w + m, y + yo + m, w - m - m, h - m - m
        )
    }

    override fun render() {
        UIRect.drawRect(x, y, width, height, color = bgColor)

        if (drawScrollbar && maxY2 > height) {
            val (x1, y1, width1, height1, x2, y2, width2, height2) = scrollbarBounds()

            UIRect.drawRect(x1, y1, width1, height1, color = scrollBgColor)
            UIRect.drawRect(x2, y2, width2, height2, color = scrollFgColor)
        }
    }

    override fun drawChildren(x2: Double, y2: Double) {
        for (child in children) {
            val h = child.hidden
            val visible = visibleComponents.contains(child)
            if (!visible) child.hidden = true
            child.draw(0.0, yOffset)
            if (!visible) child.hidden = h
        }
    }

    override fun onMouseScroll(event: UIScrollEvent) = apply {
        if (children.isEmpty()) return@apply

        val isCtrl = UITextInput.isKeyDown(InputConstants.KEY_LCONTROL)
        updateScrollY(-(if (isCtrl) 50.0 else 10.0) * event.delta.sign)
    }

    override fun onMouseClick(event: UIClickEvent) = apply {
        if (!drawScrollbar) return@apply
        val (_, _, _, _, x2, y2, width2, height2) = scrollbarBounds()
        if (event.x !in x2..x2 + width2 || event.y !in y2..y2 + height2) return@apply

        scrollBarFocused = true
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (!scrollBarFocused || !drawScrollbar) return@apply
        // TODO: if released outside bounds, it will break and leave this enabled
        scrollBarFocused = false
    }

    override fun onMouseDragOut(event: UIDragEvent) = apply {
        if (!scrollBarFocused || !drawScrollbar || maxY2 < height) return@apply
        if (event.dy == 0.0) return@apply

        val maxScroll = max(0.0, maxY2 - height)
        val scrollbarHeight = scrollbarBounds().height2
        val yScale = max(1.0, maxScroll / (height - scrollbarHeight))

        updateScrollY(event.dy * yScale)
    }

    override fun <T : UIMouseEvent> modifyChildMouseEvent(event: T) {
        event.y += yOffset
    }

    override fun <T : UIMouseEvent> resetChildMouseEvent(event: T) {
        event.y -= yOffset
    }
}
