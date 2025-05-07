package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
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
    open val children = mutableListOf<UIBase>()
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
        var onResize: ((comp: UIBase, scaledResolution: ScaledResolution) -> Unit)? = null
        var onError: ((trace: Array<out StackTraceElement>) -> Unit)? = null
        var onUpdate: (() -> Unit)? = null
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
    /**
     * * These are the animations that this [UIBase] component will handle
     * * Depending on where you want to use them or how to use them the base _should_
     * handle some of the basics for you
     */
    /** * _Should_ be used whenever the component goes left-right or right-left */
    open var xAnimation: Animation? = null
    /** * _Should_ be used whenever the component goes top-bottom or bottom-top */
    open var yAnimation: Animation? = null
    /** * _Should_ be used whenever the component grows or shrinks in width */
    open var widthAnimation: Animation? = null
    /** * _Should_ be used whenever the component grows or shrinks in height */
    open var heightAnimation: Animation? = null
    /** * Whether this component is hidden or not */
    open var hidden: Boolean = false

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

    open fun setColor(r: Int, g: Int, b: Int, a: Int = 255) = apply {
        bgColor = Color(r, g, b, a)
    }

    open fun setColor(r: Double, g: Double, b: Double, a: Double = 255.0) = apply {
        bgColor = Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
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
     * * Checks whether the specified [UIMouseEvent] is in the bounds of this component
     */
    open fun inBounds(event: UIMouseEvent): Boolean = inBounds(event.x, event.y)

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
     * * Sets the [xAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    @JvmOverloads
    open fun setXAnimation(name: String, maxTime: Float = 500f) = apply {
        xAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [yAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    open fun setYAnimation(name: String, maxTime: Float = 500f) = apply {
        yAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [widthAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    open fun setWidthAnimation(name: String, maxTime: Float = 500f) = apply {
        widthAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [heightAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    open fun setHeightAnimation(name: String, maxTime: Float = 500f) = apply {
        heightAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Checks whether this [UIBase] component is dirty
     */
    open fun isDirty(): Boolean = dirty

    /**
     * * Replaces the specified child with a new one
     * @returns a boolean that specifies whether the component was successfully replaced or not
     */
    open fun replaceChild(newComp: UIBase, oldComp: UIBase): Boolean {
        val idx = children.indexOf(oldComp)
        if (idx == -1) return false

        newComp.parent = this
        children.removeAt(idx)
        children.add(idx, newComp)
        return true
    }

    /**
     * * Inserts the specified child into the specified index
     * @returns a boolean that specifies whether the component was successfully inserted or not
     */
    open fun insertChild(comp: UIBase, idx: Int): Boolean {
        if (idx < 0 || idx > children.size) return false

        comp.parent = this
        children.add(idx, comp)
        return true
    }

    /**
     * * Sets the [hidden] variable to `true`
     */
    open fun hide() = apply {
        hidden = true
    }

    /**
     * * Sets the [hidden] variable to `false`
     */
    open fun unhide() = apply {
        hidden = false
    }

    /**
     * * Checks whether this component is the main component
     * * Usually the main component is the one that is at the top of the hierarchy
     * and thus has no parent, therefore we can do single calculations here and
     * pass them through to the children so its only done once and not per child
     */
    open fun isMainComponent(): Boolean = parent == null

    /**
     * * Gets the component that is located at the specified `x` and `y`
     * * If no component is found it will return `null`
     */
    open fun getComponentAt(x: Double, y: Double): UIBase? {
        var comp: UIBase? = null
        for (child in children) {
            if (child.inBounds(x, y)) {
                comp = child
                break
            }
        }

        return comp
    }

    /**
     * * Un-focuses the component
     */
    open fun unfocus() {
        propagateUnfocus(UIFocusEvent(-1.0, -1.0, false, this))
    }

    /**
     * * Mostly internal use but can be used to trigger the internal hook of this [UIBase]
     */
    open fun hookOnMouseClick(event: UIClickEvent) {
        hooks.onMouseClick?.invoke(event)
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
        onUpdate()
        hooks.onUpdate?.invoke()
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

    @JvmOverloads
    open fun drawChildren(x2: Double = 0.0, y2: Double = 0.0) {
        children.forEach { it.draw(x2, y2) }
    }

    @JvmOverloads
    open fun draw(x2: Double = 0.0, y2: Double = 0.0) {
        // Check the scaledResolution
        if (isMainComponent()) {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            if (scaledResolution == null) {
                scaledResolution = sr
            } else if (
                scaledResolution!!.scaledWidth != sr.scaledWidth ||
                scaledResolution!!.scaledHeight != sr.scaledHeight ||
                scaledResolution!!.scaleFactor != sr.scaleFactor) {
                scaledResolution = sr
                propagateResize(this, sr)
            }
        } else {
            scaledResolution = parent!!.scaledResolution
        }
        // Avoid doing any further computation if the component is hidden
        if (hidden) return

        if (isMainComponent()) {
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.disableCull()
        }

        try {
            // Handle mouse inputs if the component does not have a parent
            // this _should_ mean that the component is at the top of the hierarchy
            // so only this component needs to handle the inputs and pass them through
            if (isMainComponent()) handleMouseInput()
            effects.forEach { it.preDraw() }
            if (!effects.any { it.forceColor }) bgColor.bind()
            // Prepare animations here so the user does not need to do so
            xAnimation?.preDraw()
            yAnimation?.preDraw()
            widthAnimation?.preDraw()
            heightAnimation?.preDraw()
            // End
            preDraw()
            x -= x2
            y -= y2
            render()
            x += x2
            y += y2
            effects.forEach { it.preChildDraw() }
            preChildDraw()
            // If the component was marked as dirty let's update it
            if (dirty) update()
            drawChildren(x2, y2)
            effects.forEach { it.postChildDraw() }
            postChildDraw()
            effects.forEach { it.postDraw() }
            postDraw()
        } catch (e: Exception) {
            e.printStackTrace()
            propagateError(e.stackTrace)
        } finally {
            // Reset stack state only if it's the main component
            if (isMainComponent()) {
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
                GlStateManager.enableCull()
                ScissorEffect.disableScissor()
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
        propagateKeyTyped(UIKeyType(keycode, char, this))
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
            propagateMouseScroll(UIScrollEvent(mxd, myd, scroll.sign, this))

        // Handle mouseEnter/Hover/Leave
        if (insideBounds) {
            propagateMouseEnter(UIMouseEvent(mxd, myd, this))
            propagateMouseHover(UIMouseEvent(mxd, myd, this))
        }
        propagateMouseLeave(UIMouseEvent(mxd, myd, this))

        mouseInBounds = insideBounds

        // Handle mouse click/release/drag
        for (btn in 0..Mouse.getButtonCount()) {
            val oldState = mouseState[btn] ?: false
            val btnState = Mouse.isButtonDown(btn)
            if (oldState != btnState) {
                mouseState[btn] = btnState

                if (insideBounds) {
                    if (oldState) propagateMouseRelease(UIClickEvent(mxd, myd, btn, this))
                    else {
                        propagateMouseClick(UIClickEvent(mxd, myd, btn, this))
                        propagateFocus(UIFocusEvent(mxd, myd, true, this))
                    }
                }
                if (focused && !insideBounds) {
                    focused = false
                    propagateUnfocus(UIFocusEvent(mxd, myd, focused, this))
                } else {
                    val event = UIFocusEvent(mxd, myd, false, this)
                    for (child in children) {
                        if (!child.focused || child.inBounds(event)) continue

                        child.focused = false
                        child.propagateUnfocus(event)
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
                propagateMouseDrag(UIDragEvent(
                    mxd - state.x,
                    myd - state.y,
                    mxd,
                    myd,
                    btn,
                    this)
                )
            }

            draggedState[btn] = State(mxd, myd)
        }
    }

    open fun propagateMouseScroll(event: UIScrollEvent) {
        onMouseScroll(event)
        hooks.onMouseScroll?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.inBounds(event)) continue

            child.propagateMouseScroll(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseClick(event: UIClickEvent) {
        onMouseClick(event)
        hooks.onMouseClick?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.inBounds(event)) continue

            child.propagateMouseClick(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseRelease(event: UIClickEvent) {
        onMouseRelease(event)
        hooks.onMouseRelease?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.inBounds(event)) continue

            child.propagateMouseRelease(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseEnter(event: UIMouseEvent) {
        if (!mouseInBounds) {
            onMouseEnter(event)
            hooks.onMouseEnter?.invoke(event)
            mouseInBounds = true
        }
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.mouseInBounds) continue

            child.propagateMouseEnter(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseLeave(event: UIMouseEvent) {
        if (mouseInBounds && !inBounds(event)) {
            onMouseLeave(event)
            hooks.onMouseLeave?.invoke(event)
            mouseInBounds = false
        }
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.mouseInBounds || child.inBounds(event)) continue

            child.propagateMouseLeave(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseHover(event: UIMouseEvent) {
        onMouseHover(event)
        hooks.onMouseHover?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.inBounds(event)) continue

            child.propagateMouseHover(event)
            if (!event.propagate) break
        }
    }

    open fun propagateMouseDrag(event: UIDragEvent) {
        onMouseDrag(event)
        hooks.onMouseDrag?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            // TODO: fix me
            child.onMouseDragOut(event)
            if (!child.inBounds(event)) continue

            child.propagateMouseDrag(event)
            if (!event.propagate) break
        }
    }

    open fun propagateFocus(event: UIFocusEvent) {
        if (focused != event.state) {
            onFocus(event)
            hooks.onFocus?.invoke(event)
            focused = event.state
        }
        if (!event.propagate) return

        for (child in children.toList()) {
            if (child.focused || !child.inBounds(event)) continue

            child.propagateFocus(event)
            if (!event.propagate) break
        }
    }

    open fun propagateUnfocus(event: UIFocusEvent) {
        onUnfocus(event)
        onLostFocus(event)
        hooks.onUnfocus?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.focused) continue

            child.focused = focused
            child.propagateUnfocus(event)
            if (!event.propagate) break
        }
    }

    open fun propagateKeyTyped(event: UIKeyType) {
        onKeyTyped(event)
        onKeyType(event)
        hooks.onKeyType?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.focused) continue

            child.propagateKeyTyped(event)
            if (!event.propagate) break
        }
    }

    open fun propagateResize(comp: UIBase, scaledResolution: ScaledResolution) {
        markDirty()
        onResize(comp, scaledResolution)
        hooks.onResize?.invoke(comp, scaledResolution)

        for (child in children.toList()) child.propagateResize(comp, scaledResolution)
    }

    open fun propagateError(trace: Array<out StackTraceElement>) {
        onError(trace)
        hooks.onError?.invoke(trace)

        for (child in children.toList()) child.propagateError(trace)
    }

    open fun onResize(comp: UIBase, scaledResolution: ScaledResolution) = apply {}
    open fun onResize(cb: (comp: UIBase, scaledResolution: ScaledResolution) -> Unit) = apply {
        hooks.onResize = cb
    }
    open fun onError(trace: Array<out StackTraceElement>) = apply {}
    open fun onError(cb: (trace: Array<out StackTraceElement>) -> Unit) = apply {
        hooks.onError = cb
    }

    open fun onMouseClick(event: UIClickEvent) = apply {}
    open fun onMouseClick(cb: (event: UIClickEvent) -> Unit) = apply {
        hooks.onMouseClick = cb
    }
    open fun onMouseDrag(event: UIDragEvent) = apply {}
    open fun onMouseDrag(cb: (event: UIDragEvent) -> Unit) = apply {
        hooks.onMouseDrag = cb
    }
    /**
     * * Triggers whenever the mouse is dragged inside the parent component
     * but the drag was started inside `this` component
     */
    open fun onMouseDragOut(event: UIDragEvent) = apply {}
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

    open fun onUpdate() = apply {}
    open fun onUpdate(cb: () -> Unit) = apply {
        hooks.onUpdate = cb
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
