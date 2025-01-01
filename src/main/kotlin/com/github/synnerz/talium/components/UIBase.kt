package com.github.synnerz.talium.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * * Base component that every other component _should_ extend to
 * * This component handles the mouse and keyboard events as well as updating the
 * position of the children and itself
 * @param _x X position in percent (0-100)
 * @param _y Y position in percent (0-100)
 * @param _width Width size in percent (0-100)
 * @param _height Height size in percent (0-100)
 * @param parent The parent of this component (can be left as `null`)
 */
open class UIBase @JvmOverloads constructor(
    var _x: Double,
    var _y: Double,
    var _width: Double,
    var _height: Double,
    var parent: UIBase? = null
) {
    private val children = mutableListOf<UIBase>()
    /**
     * * This is a list of effects that the current component uses
     * * i.e. OutlineEffect
     */
    private val effects = mutableListOf<Any>()
    /**
     * * These are listeners made by the component itself
     */
    private val listeners = object {}
    /**
     * * These are the same as [listeners] but made by the user
     * * i.e. if i want to listen for a mouseClick on a component, i'll add a click hook
     */
    private val hooks = object {}
    /**
     * * Field to check whether this component is dirty or not
     * * When a component is marked as dirty this means that
     * they need to be updated in the size, position and as well as their children's size and position
     * * i.e. if the window is resized this _should_ be marked as dirty, so it can recalculate the position etc
     */
    private var dirty: Boolean = true
    /**
     * * Used internally to scale the position and size of the component
     * as well as to trigger the [onResize] hook/listener
     */
    private var scaledResolution: ScaledResolution? = null
    /** * Note: if you call the setter it will not mark the component as dirty */
    var x: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var y: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var width: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var height: Double = 0.0
    var bounds: Boundaries = Boundaries(-1.0, -1.0, -1.0, -1.0)
    var bgColor: Color = Color(0, 0, 0, 0)

    init {
        // Adds [this] component as a children for the specified parent
        parent?.children?.add(this)
    }

    /**
     * * Sets the [dirty] variable of this component to the specified state
     */
    @JvmOverloads
    open fun setDirty(state: Boolean = true) = apply {
        dirty = state
    }

    /**
     * * Marks this component as dirty, so it can recalculate positions next render
     */
    open fun markDirty() = apply {
        dirty = true
    }

    open fun setColor(color: Color) = apply {
        bgColor = color
    }

    /**
     * * Adds the specified [child] to this component
     * * Note: if the component already has a parent it will be removed and re-assigned to this one
     */
    open fun addChild(child: UIBase) = apply {
        val oldParent = child.parent
        if (oldParent != null) {
            if (oldParent == this) return@apply
            oldParent.removeChild(child)
        }
        children.add(child)
        child.parent = this
        markDirty()
    }

    /**
     * * Checks whether the specified component is a child of this component
     */
    open fun hasChild(child: UIBase): Boolean = children.contains(child)

    /**
     * * Checks whether this component is a child of the specified [parent] component
     */
    @JvmOverloads
    open fun hasParent(parent: UIBase? = null): Boolean {
        val comp = parent ?: this.parent
        if (comp == null) return false
        return comp == this.parent
    }

    /**
     * * Sets this component as a child of the specified [parent] component
     */
    open fun setChildOf(parent: UIBase) = apply {
        if (parent.hasChild(this)) return@apply
        parent.addChild(this)
    }

    /**
     * * Removes the specified child from this component
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    open fun removeChild(child: UIBase): Boolean {
        val removed = children.remove(child)
        markDirty()
        return removed
    }

    /**
     * * Removes this component from its parent
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    open fun remove(): Boolean = parent?.removeChild(this) ?: false

    /**
     * * Clears all the children from this component
     * * Removes this component as their parent as well as marking them dirty
     */
    open fun clearChildren() = apply {
        children.forEach {
            it.parent = null
            it.markDirty()
        }
        children.clear()
        markDirty()
    }

    /**
     * * Sets the position for this component
     * * Note: this marks the component as dirty
     * @param x The X position in percent (`0-100`)
     * @param y The Y position in percent (`0-100`)
     */
    open fun setPos(x: Double, y: Double) = apply {
        _x = x
        _y = y
        markDirty()
    }

    /**
     * * Sets the position for this component
     * * Note: this marks the component as dirty
     * @param x The X position in percent (`0-100`)
     * @param y The Y position in percent (`0-100`)
     */
    open fun setPosition(x: Double, y: Double) = setPos(x, y)

    /**
     * * Sets the size for this component
     * * Note: this marks the component as dirty
     * @param width The Width for this component in percent (`0-100`)
     * @param height The Height for this component in percent (`0-100`)
     */
    open fun setSize(width: Double, height: Double) = apply {
        _width = width
        _height = height
        markDirty()
    }

    /**
     * * Checks whether the specified [x] and [y] are in the bounds of this component
     * * Note: if the component's bounds have not yet been set it will return `false`
     */
    open fun inBounds(x: Double, y: Double): Boolean {
        if (bounds.x1 == -1.0) return false
        return x in bounds.x1..bounds.x2 && y in bounds.y1..bounds.y2
    }

    /**
     * * This is the update method, whenever the [dirty] variable is set to true
     * this method gets called in rendering
     * * This is mostly used internally to update size, position and children size and position
     */
    open fun update() = apply {
        val parentX = parent?.x ?: 0.0
        val parentY = parent?.y ?: 0.0
        val parentWidth = parent?.width ?: scaledResolution?.scaledWidth_double ?: 0.0
        val parentHeight = parent?.height ?: scaledResolution?.scaledHeight_double ?: 0.0

        dirty = false
        x = _x / 100 * parentWidth + parentX
        y = _y / 100 * parentHeight + parentY
        width = _width / 100 * parentWidth
        height = _height / 100 * parentHeight
        bounds = Boundaries(x, y, x + width, y + height)
    }

    /**
     * * Override this method if you need to do something **before** the component is drawn
     */
    open fun preDraw() {}
    /**
     * * Override this method if you need to do something **after** the component is drawn
     */
    open fun postDraw() {}
    /**
     * * Override this method if you need to do something **before** the children are drawn
     */
    open fun preChildDraw() {}
    /**
     * * Override this method if you need to do something **after** the children are drawn
     */
    open fun postChildDraw() {}
    /**
     * * Override this method to draw your custom component
     */
    open fun render() {}

    open fun draw() {
        // Check the scaledResolution
        val sr = ScaledResolution(Minecraft.getMinecraft())
        if (scaledResolution == null) {
            scaledResolution = sr
        } else if(
            scaledResolution!!.scaledWidth != sr.scaledWidth &&
            scaledResolution!!.scaledHeight != sr.scaledHeight &&
            scaledResolution!!.scaleFactor != sr.scaleFactor) {
            scaledResolution = sr
            handleResize(this, sr)
        }

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableCull()
        GlStateManager.color(bgColor.red.toFloat(), bgColor.green.toFloat(), bgColor.blue.toFloat(), bgColor.alpha.toFloat())

        try {
            // TODO: add effect pre/post draws
            // TODO: add mouse and keyboard event handlers here
            preDraw()
            render()
            preChildDraw()
            // If the component was marked as dirty let's update it
            if (dirty) update()
            children.forEach { it.draw() }
            postChildDraw()
            postDraw()
        } catch (e: Exception) {
            e.printStackTrace()
            handleError(e.stackTrace)
        } finally {
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.enableCull()
        }
    }

    open fun handleResize(comp: UIBase, scaledResolution: ScaledResolution) {
        onResize(comp, scaledResolution)
        children.forEach { it.onResize(comp, scaledResolution) }
    }
    open fun handleError(trace: Array<out StackTraceElement>) {
        onError(trace)
        children.forEach { it.onError(trace) }
    }
    open fun handleMouseInputs() {}
    open fun onResize(comp: UIBase, scaledResolution: ScaledResolution) = apply {}
    open fun onError(trace: Array<out StackTraceElement>) = apply {}

    /**
     * * This class represents the current boundaries of the component
     * @param x1 X position in pixels
     * @param y1 Y position in pixels
     * @param x2 X + Width position in pixels
     * @param y2 Y + Height position in pixels
     */
    data class Boundaries(val x1: Double, val y1: Double, val x2: Double, val y2: Double)
}
