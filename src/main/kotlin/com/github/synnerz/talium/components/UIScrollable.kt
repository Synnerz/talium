package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.events.*
import com.github.synnerz.talium.utils.Renderer.stack
import org.lwjgl.glfw.GLFW
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
    private var visibleComponents = listOf<UIElement>()
    var drawScrollbar: Boolean = false
    var scrollBgColor = Color(241, 241, 241, 255)
    var scrollFgColor = Color(136, 136, 136, 255)

    init {
        addEffect(ScissorEffect())
    }

    override fun onUpdate() = apply {
        children.forEach { if (it.isDirty()) it.update() }
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
        }
    }

    override fun render() {
        UIRect.drawRect(x, y, width, height, color = bgColor)

        if (drawScrollbar && maxY2 > height) {
            val w = min(5.0, width * 0.1)
            val h = (height / maxY2) * height
            val yo = (yOffset / maxY2) * height
            val m = w * 0.075
            UIRect.drawRect(x + width - w, y, w, height, color = scrollBgColor)
            UIRect.drawRect(x + width - w + m, y + yo + m, w - m - m, h - m - m, color = scrollFgColor)
        }
    }

    override fun drawChildren(x2: Double, y2: Double) {
        for (child in visibleComponents) {
            child.draw(0.0, yOffset)
        }
    }

    override fun onMouseScroll(event: UIScrollEvent) = apply {
        if (children.isEmpty()) return@apply

        val isCtrl = UITextInput.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)
        updateScrollY(-(if (isCtrl) 50.0 else 10.0) * event.delta.sign)
    }

    override fun <T : UIMouseEvent> modifyChildMouseEvent(event: T) {
        event.y += yOffset
    }

    override fun <T : UIMouseEvent> resetChildMouseEvent(event: T) {
        event.y -= yOffset
    }
}
