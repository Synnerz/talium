package com.github.synnerz.talium.components

import com.github.synnerz.talium.effects.OutlineEffect
import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.effects.UIEffect
import com.github.synnerz.talium.events.*
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.Renderer.bind
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.sign

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
     * * i.e. [OutlineEffect]
     */
    private val effects = mutableListOf<UIEffect>()
    /**
     * * These are listeners made by the user
     * * i.e. if i want to listen for a mouseClick on a component, i'll add a click hook
     */
    private val hooks = object {
        var onMouseScroll: ((event: UIScrollEvent) -> Unit)? = null
        var onMouseClick: ((event: UIClickEvent) -> Unit)? = null
        var onMouseRelease: ((event: UIClickEvent) -> Unit)? = null
        var onMouseEnter: ((event: UIMouseEvent) -> Unit)? = null
        var onMouseHover: ((event: UIMouseEvent) -> Unit)? = null
        var onMouseLeave: ((event: UIMouseEvent) -> Unit)? = null
        var onMouseDrag: ((event: UIDragEvent) -> Unit)? = null
        var onFocus: ((event: UIFocusEvent) -> Unit)? = null
        var onUnfocus: ((event: UIFocusEvent) -> Unit)? = null
        var onKeyType: ((event: UIKeyType) -> Unit)? = null
    }
    /**
     * * Field to check whether this component is dirty or not
     * * When a component is marked as dirty this means that
     * they need to be updated in the size, position and as well as their children's size and position
     * * i.e. if the window is resized this _should_ be marked as dirty, so it can recalculate the position etc
     */
    private var dirty: Boolean = true
    private var mouseInBounds: Boolean = false
    private val mouseState = mutableMapOf<Int, Boolean>()
    private val draggedState = mutableMapOf<Int, State>()
    /**
     * * Used internally to scale the position and size of the component
     * as well as to trigger the [onResize] hook/listener
     */
    var scaledResolution: ScaledResolution? = null
        internal set
    /** * Note: if you call the setter it will not mark the component as dirty */
    var x: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var y: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var width: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    var height: Double = 0.0
    var bounds: Boundaries = Boundaries(-1.0, -1.0, -1.0, -1.0)
    open var bgColor: Color = Color(0, 0, 0, 0)
    /** * Variable that lets the component be known if its focused or not, mostly used for keyboard inputs */
    open var focused: Boolean = false

    data class State(var x: Double, var y: Double)

    init {
        // Adds [this] component as a children for the specified parent
        parent?.children?.add(this)
    }

    /**
     * * Sets the [dirty] variable of this component to the specified state
     */
    @JvmOverloads
    open fun setDirty(state: Boolean = true): UIBase = apply {
        dirty = state
        children.forEach { it.setDirty(state) }
    }

    /**
     * * Marks this component as dirty, so it can recalculate positions next render
     */
    open fun markDirty(): UIBase = apply {
        dirty = true
        children.forEach { it.markDirty() }
    }

    /**
     * * Sets the color of this component
     * * Note: if there is a color effect it will override this color
     */
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
     * * Adds a single [UIEffect] to this component
     */
    open fun addEffect(effect: UIEffect) = apply {
        effect.component = this
        effects.add(effect)
    }

    /**
     * * Adds multiple [UIEffect] to this component
     */
    open fun addEffects(vararg effects: UIEffect) = apply {
        effects.forEach { it.component = this }
        this.effects.addAll(effects)
    }

    /**
     * * Removes the specified [effect] from this component
     */
    open fun removeEffect(effect: UIEffect): Boolean = effects.remove(effect)

    /**
     * * Removes the [UIEffect]s that are instance of the specified [clazz]
     */
    open fun <T: UIEffect> removeEffects(clazz: Class<T>): Boolean = effects.removeIf { clazz.isInstance(it) }

    /**
     * * Checks whether this component's [focused] variable is true or false
     */
    open fun hasFocus(): Boolean = focused

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
        } else if (
            scaledResolution!!.scaledWidth != sr.scaledWidth ||
            scaledResolution!!.scaledHeight != sr.scaledHeight ||
            scaledResolution!!.scaleFactor != sr.scaleFactor) {
            scaledResolution = sr
            handleResize(this, sr)
        }

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableCull()

        try {
            // Handle mouse inputs if the component does not have a parent
            // this _should_ mean that the component is at the top of the hierarchy
            // so only this component needs to handle the inputs and pass them through
            if (parent == null) handleMouseInput()
            effects.forEach { it.preDraw() }
            if (!effects.any { it.forceColor }) bgColor.bind()
            preDraw()
            render()
            effects.forEach { it.preChildDraw() }
            preChildDraw()
            // If the component was marked as dirty let's update it
            if (dirty) update()
            children.forEach { it.draw() }
            effects.forEach { it.postChildDraw() }
            postChildDraw()
            effects.forEach { it.postDraw() }
            postDraw()
        } catch (e: Exception) {
            e.printStackTrace()
            handleError(e.stackTrace)
        } finally {
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.enableCull()
            if (ScissorEffect.scissorState) {
                ScissorEffect.scissorState = false
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
            }
        }
    }

    /**
     * * Call this method inside a [GuiScreen]'s [GuiScreen.keyTyped]
     * this will handle all the keytyped as well as only trigger if it's the highest component
     * in the hierarchy
     */
    open fun handleKeyInput(keycode: Int, char: Char) {
        if (parent != null || !focused) return
        propagateKeyTyped(keycode, char)
    }

    open fun handleMouseInput() {
        if (scaledResolution == null) return

        val mx = Renderer.getMouseX(scaledResolution!!)
        val my = Renderer.getMouseY(scaledResolution!!)
        val mxd = mx.toDouble()
        val myd = my.toDouble()
        val insideBounds = inBounds(mxd, myd)

        // Handle scroll
        val scroll = Mouse.getDWheel()
        if (scroll != 0 && insideBounds)
            propagateMouseScroll(mxd, myd, scroll.sign)

        // Handle mouseEnter/Hover/Leave
        if (insideBounds) {
            if (!mouseInBounds) propagateMouseEnter(mxd, myd)
            propagateMouseHover(mxd, myd)
        } else if (mouseInBounds) propagateMouseLeave(mxd, myd)

        mouseInBounds = insideBounds

        // Handle mouse click/release/drag
        // without chick it couldn't be
        for (btn in 0..Mouse.getButtonCount()) {
            val oldState = mouseState[btn] ?: false
            val btnState = Mouse.isButtonDown(btn)
            if (oldState != btnState) {
                mouseState[btn] = btnState

                if (insideBounds) {
                    if (oldState) propagateMouseRelease(mxd, myd, btn)
                    else {
                        propagateMouseClick(mxd, myd, btn)
                        propagateFocus(true, mxd, myd)
                    }
                }
                if (focused && !insideBounds) {
                    focused = false
                    propagateUnfocus(mxd, myd)
                } else {
                    val event = UIFocusEvent(false, this)
                    for (child in children) {
                        if (!child.focused || child.inBounds(mxd, myd)) continue
                        child.focused = false
                        child.onUnfocus(event)
                        child.onLostFocus(event)
                        child.hooks.onUnfocus?.invoke(event)
                        if (!event.propagate) break
                    }
                }

                if (btnState) draggedState[btn] = State(mxd, myd)
                else draggedState.remove(btn)
            }

            if (btn !in draggedState) continue
            val state = draggedState[btn]
            if (state!!.x == mxd && state.y == myd) continue

            if (insideBounds) {
                propagateMouseDrag(
                    x - state.x,
                    y - state.y,
                    x,
                    y,
                    btn
                )
            }

            draggedState[btn] = State(mxd, myd)
        }
    }

    open fun propagateMouseScroll(x: Double, y: Double, delta: Int) {
        val event = UIScrollEvent(x, y, delta, this)
        onMouseScroll(event)
        hooks.onMouseScroll?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseScroll(event)
            child.hooks.onMouseScroll?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseClick(x: Double, y: Double, button: Int) {
        val event = UIClickEvent(x, y, button, this)
        onMouseClick(event)
        hooks.onMouseClick?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseClick(event)
            child.hooks.onMouseClick?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseRelease(x: Double, y: Double, button: Int) {
        val event = UIClickEvent(x, y, button, this)
        onMouseRelease(event)
        hooks.onMouseRelease?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseRelease(event)
            child.hooks.onMouseRelease?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseEnter(x: Double, y: Double) {
        val event = UIMouseEvent(x, y, this)
        onMouseEnter(event)
        hooks.onMouseEnter?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseEnter(event)
            child.hooks.onMouseEnter?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseLeave(x: Double, y: Double) {
        val event = UIMouseEvent(x, y, this)
        onMouseLeave(event)
        hooks.onMouseLeave?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseLeave(event)
            child.hooks.onMouseLeave?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseHover(x: Double, y: Double) {
        val event = UIMouseEvent(x, y, this)
        onMouseHover(event)
        hooks.onMouseHover?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseHover(event)
            child.hooks.onMouseHover?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseDrag(dx: Double, dy: Double, x: Double, y: Double, button: Int) {
        val event = UIDragEvent(dx, dy, x, y, button, this)
        onMouseDrag(event)
        hooks.onMouseDrag?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.inBounds(x, y)) continue
            child.onMouseDrag(event)
            child.hooks.onMouseDrag?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateFocus(state: Boolean, x: Double, y: Double) {
        val event = UIFocusEvent(state, this)
        if (focused != state) {
            onFocus(event)
            hooks.onFocus?.invoke(event)
            focused = state
        }
        if (!event.propagate) return
        for (child in children) {
            if (child.focused || !child.inBounds(x, y)) continue
            child.focused = focused
            child.onFocus(event)
            child.hooks.onFocus?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateUnfocus(x: Double, y: Double) {
        val event = UIFocusEvent(focused, this)
        onUnfocus(event)
        onLostFocus(event)
        hooks.onUnfocus?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.focused) continue
            child.focused = focused
            child.onUnfocus(event)
            child.onLostFocus(event)
            child.hooks.onUnfocus?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun propagateKeyTyped(keycode: Int, char: Char) {
        val event = UIKeyType(keycode, char, this)
        onKeyTyped(event)
        onKeyType(event)
        hooks.onKeyType?.invoke(event)
        if (!event.propagate) return
        for (child in children) {
            if (!child.focused) continue
            child.onKeyTyped(event)
            child.onKeyType(event)
            child.hooks.onKeyType?.invoke(event)
            if (!event.propagate) break
        }
    }

    open fun handleResize(comp: UIBase, scaledResolution: ScaledResolution) {
        markDirty()
        onResize(comp, scaledResolution)
        children.forEach { it.onResize(comp, scaledResolution) }
    }
    open fun handleError(trace: Array<out StackTraceElement>) {
        onError(trace)
        children.forEach { it.onError(trace) }
    }
    open fun onResize(comp: UIBase, scaledResolution: ScaledResolution) = apply {}
    open fun onError(trace: Array<out StackTraceElement>) = apply {}

    open fun onMouseClick(event: UIClickEvent) = apply {}
    open fun onMouseClick(cb: (event: UIClickEvent) -> Unit) = apply {
        hooks.onMouseClick = cb
    }
    open fun onMouseDrag(event: UIDragEvent) = apply {}
    open fun onMouseDrag(cb: (event: UIDragEvent) -> Unit) = apply {
        hooks.onMouseDrag = cb
    }
    open fun onMouseRelease(event: UIClickEvent) = apply {}
    open fun onMouseRelease(cb: (event: UIClickEvent) -> Unit) = apply {
        hooks.onMouseRelease = cb
    }
    open fun onMouseEnter(event: UIMouseEvent) = apply {}
    open fun onMouseEnter(cb: (event: UIMouseEvent) -> Unit) = apply {
        hooks.onMouseEnter = cb
    }
    open fun onMouseHover(event: UIMouseEvent) = apply {}
    open fun onMouseHover(cb: (event: UIMouseEvent) -> Unit) = apply {
        hooks.onMouseHover = cb
    }
    open fun onMouseLeave(event: UIMouseEvent) = apply {}
    open fun onMouseLeave(cb: (event: UIMouseEvent) -> Unit) = apply {
        hooks.onMouseLeave = cb
    }
    open fun onMouseScroll(event: UIScrollEvent) = apply {}
    open fun onMouseScroll(cb: (event: UIScrollEvent) -> Unit) = apply {
        hooks.onMouseScroll = cb
    }
    open fun onFocus(event: UIFocusEvent) = apply {}
    open fun onFocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hooks.onFocus = cb
    }
    open fun onUnfocus(event: UIFocusEvent) = apply {}
    open fun onUnfocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hooks.onUnfocus = cb
    }
    open fun onLostFocus(event: UIFocusEvent) = apply {}
    open fun onLostFocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hooks.onUnfocus = cb
    }

    open fun onKeyType(event: UIKeyType) = apply {}
    open fun onKeyType(cb: (event: UIKeyType) -> Unit) = apply {
        hooks.onKeyType = cb
    }
    open fun onKeyTyped(event: UIKeyType) = apply {}
    open fun onKeyTyped(cb: (event: UIKeyType) -> Unit) = apply {
        hooks.onKeyType = cb
    }

    /**
     * * This class represents the current boundaries of the component
     * @param x1 X position in pixels
     * @param y1 Y position in pixels
     * @param x2 X + Width position in pixels
     * @param y2 Y + Height position in pixels
     */
    data class Boundaries(val x1: Double, val y1: Double, val x2: Double, val y2: Double)
}
