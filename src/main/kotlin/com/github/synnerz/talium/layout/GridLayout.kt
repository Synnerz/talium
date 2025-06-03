package com.github.synnerz.talium.layout

import com.github.synnerz.talium.components.UIBase

open class GridLayout @JvmOverloads constructor(
    var columns: Int,
    var xPadding: Double = 10.0,
    var yPadding: Double = 5.0
) : Layout() {
    var grid: MutableList<GridComp> = mutableListOf()

    data class GridComp(
        val xOffset: Double = 0.0,
        val yOffset: Double = 0.0,
        val comp: UIBase,
        var offset: Boolean = false
    )

    override fun onUpdate() {
        val columnWidth = (parent!!.width / columns) - xPadding
        var columnHeight = 0.0
        var currentCol = 0
        var yOffset = 0.0
        grid.clear()

        for (child in parent!!.children.toList()) {
            if (child.isDirty()) child.update()

            if (currentCol >= columns) {
                yOffset = (yOffset + (columnHeight + yPadding)).coerceAtMost(parent!!.height)
                currentCol = 0
                columnHeight = 0.0
            }

            val xOffset = columnWidth * currentCol
            grid.add(GridComp(xOffset, yOffset, child))
            columnHeight = maxOf(columnHeight, child.height)
            currentCol++
        }
    }

    override fun preChildDraw() {
        grid.toList().forEach {
            val comp = it.comp
            if (!it.offset) {
                comp.x += it.xOffset
                comp.y += it.yOffset
                comp.bounds = UIBase.Boundaries(comp.x, comp.y, comp.x + comp.width, comp.y + comp.height)
            }
            it.offset = true
        }
    }
}