package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.utils.Renderer
import java.awt.Color
import kotlin.math.max

open class UIDropDown @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var value: Int = 0,
    val options: List<String> = listOf(),
    parent: UIElement? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var arrowToggle = false
    open val arrowRect = UIRect(85.0, 2.0, 15.0, 100.0, parent = this)
    open val arrowText = UIText(0.0, 0.0, 100.0, 100.0, "↓", true, arrowRect).apply {
        textScale = 2f
    }
    open val currentText = UIText(0.0, 0.0, 85.0, 100.0, options[value], true, this)
    val floatingChild = object : FloatingUI(this) {
        override fun setFloatingPos(parent: UIElement) {
            x = parent.x
            y = parent.y + parent.height * 1.01
            val dim = max(parent.width, parent.height)
            width = dim * 1.03
            height = dim * 1.05
        }
    }.also { it.hide() }
    val floatingBg = UIRect(0.0, 0.0, 100.0, 100.0, parent = floatingChild)
    val scrollable = UIScrollable(0.0, 0.0, 100.0, 100.0, parent = floatingBg)
    override var bgColor: Color = Color(0, 0, 0, 0)
        set(value) {
            floatingBg.setColor(value)
            scrollable.children.forEach { it.setColor(value.brighter()) }
            field = value
        }

    init {
        options.forEachIndexed { idx, it ->
            UIRect(1.0, 1.0 + idx * 16.0, 99.0, 15.0, parent = scrollable).apply {
                setColor(bgColor.brighter())
                addChild(UIText(0.0, 0.0, 100.0, 100.0, it, true))
                onMouseRelease { event ->
                    if (event.button != 0) return@onMouseRelease
                    setOption(idx)
                    hideDropdown()
                }
            }
        }
    }

    override fun render() {
        Renderer.drawRect(x, y, width, height, color = bgColor)
    }

    open fun hideDropdown() {
        floatingChild.hide()
        arrowText.text = "↓"
        arrowToggle = false
    }

    open fun unhideDropdown() {
        floatingChild.unhide()
        arrowText.text = "↑"
        arrowToggle = true
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (event.button != 0) return@apply

        if (!arrowToggle) {
            unhideDropdown()
            return@apply
        }

        hideDropdown()
    }

    open fun setOption(idx: Int) {
        value = idx.coerceIn(0, options.lastIndex)
        currentText.text = options[value]
    }
}