package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.animations.Animations
import com.github.synnerz.talium.constraints.UIHeightConstraint
import com.github.synnerz.talium.constraints.UIWidthConstraint
import com.github.synnerz.talium.constraints.UIXConstraint
import com.github.synnerz.talium.constraints.UIYConstraint
import com.github.synnerz.talium.effects.OutlineEffect
import com.github.synnerz.talium.effects.ScissorEffect
import com.github.synnerz.talium.effects.UIEffect
import com.github.synnerz.talium.events.*
import com.github.synnerz.talium.layout.Layout
import com.github.synnerz.talium.utils.MouseState
import com.github.synnerz.talium.utils.Renderer
import com.github.synnerz.talium.utils.ScaledResolution
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
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
    override var _x: Double,
    override var _y: Double,
    override var _width: Double,
    override var _height: Double,
    override var parent: UIElement? = null
) : UIElement {
    override val children = mutableListOf<UIElement>()
    override val floatingChildren = mutableListOf<FloatingUI>()

    /**
     * * This is a list of effects that the current component uses
     * * i.e. [OutlineEffect]
     */
    private val effects = mutableListOf<UIEffect>()
    /**
     * * These are listeners made by the user
     * * i.e. if i want to listen for a mouseClick on a component, i'll add a click hook
     */
    override var hookMouseScroll: ((event: UIScrollEvent) -> Unit)? = null
    override var hookMouseClick: ((event: UIClickEvent) -> Unit)? = null
    override var hookMouseRelease: ((event: UIClickEvent) -> Unit)? = null
    override var hookMouseEnter: ((event: UIMouseEvent) -> Unit)? = null
    override var hookMouseHover: ((event: UIMouseEvent) -> Unit)? = null
    override var hookMouseLeave: ((event: UIMouseEvent) -> Unit)? = null
    override var hookMouseDrag: ((event: UIDragEvent) -> Unit)? = null
    override var hookFocus: ((event: UIFocusEvent) -> Unit)? = null
    override var hookUnfocus: ((event: UIFocusEvent) -> Unit)? = null
    override var hookKeyType: ((event: UIKeyType) -> Unit)? = null
    override var hookCharType: ((event: UICharEvent) -> Unit)? = null
    override var hookResize: ((comp: UIElement, scaledResolution: ScaledResolution) -> Unit)? = null
    override var hookError: ((trace: Array<out StackTraceElement>) -> Unit)? = null
    override var hookUpdate: (() -> Unit)? = null
    /** * Runs before the event is passed through to the children of this component */
    override var preChildPropagate: ((event: UIMouseEvent) -> Unit)? = null
    /** * Runs after the event is passed through to the children of this component */
    override var postChildPropagate: ((event: UIMouseEvent) -> Unit)? = null
    /**
     * * Field to check whether this component is dirty or not
     * * When a component is marked as dirty this means that
     * they need to be updated in the size, position and as well as their children's size and position
     * * i.e. if the window is resized this _should_ be marked as dirty, so it can recalculate the position etc
     */
    override var isSelfDirty: Boolean = true
    override var isChildDirty: Boolean = false
    override var mouseInBounds: Boolean = false
    private val mouseState = mutableMapOf<Int, Boolean>()
    private val draggedState = mutableMapOf<Int, State>()
    /**
     * * Used internally to scale the position and size of the component
     * as well as to trigger the [onResize] hook/listener
     */
    override var scaledResolution: ScaledResolution? = null
    /** * Note: if you call the setter it will not mark the component as dirty */
    override var x: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    override var y: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    override var width: Double = 0.0
    /** * Note: if you call the setter it will not mark the component as dirty */
    override var height: Double = 0.0
    override var bounds: UIElement.Boundaries = UIElement.Boundaries(-1.0, -1.0, -1.0, -1.0)
    override var bgColor: Color = Color(0, 0, 0, 0)
    /** * Variable that lets the component be known if its focused or not, mostly used for keyboard inputs */
    override var focused: Boolean = false
    /**
     * * These are the animations that this [UIElement] component will handle
     * * Depending on where you want to use them or how to use them the base _should_
     * handle some of the basics for you
     */
    /** * _Should_ be used whenever the component goes left-right or right-left */
    override var xAnimation: Animation? = null
    /** * _Should_ be used whenever the component goes top-bottom or bottom-top */
    override var yAnimation: Animation? = null
    /** * _Should_ be used whenever the component grows or shrinks in width */
    override var widthAnimation: Animation? = null
    /** * _Should_ be used whenever the component grows or shrinks in height */
    override var heightAnimation: Animation? = null
    /** * Whether this component is hidden or not */
    override var hidden: Boolean = false
    /** * The layout to use for the children drawing */
    override var layout: Layout? = null
    /** * Workaround for scroll wheel event **/
    private var hasScrollListener = false
    override var xConstraint: UIXConstraint? = null
    override var yConstraint: UIYConstraint? = null
    override var widthConstraint: UIWidthConstraint? = null
    override var heightConstraint: UIHeightConstraint? = null

    init {
        // Adds [this] component as a children for the specified parent
        parent?.children?.add(this)
    }

    /**
     * * Sets the [isSelfDirty] variable of this component to the specified state
     */
    override fun setDirty(state: Boolean): UIElement = apply {
        isSelfDirty = state
        children.forEach { it.setDirty(state) }
        floatingChildren.forEach { it.setDirty(state) }
    }

    /**
     * * Marks this component as dirty, so it can recalculate positions next render
     */
    override fun markDirty(): UIElement = apply {
        isSelfDirty = true
        children.forEach { it.markDirty() }
        floatingChildren.forEach { it.markDirty() }
        parent?.markChildDirty()
    }

    override fun markChildDirty(): UIElement = apply {
        isChildDirty = true
        parent?.markChildDirty()
    }

    /**
     * * Sets the color of this component
     * * Note: if there is a color effect it will override this color
     */
    override fun setColor(color: Color) = apply {
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
    override fun addChild(child: UIElement) = apply {
        val oldParent = child.parent
        if (oldParent != null) {
            if (oldParent == this) return@apply
            oldParent.removeChild(child)
        }
        children.add(child)
        child.parent = this
        markDirty()
    }

    override fun addFloatingChild(child: FloatingUI) = apply {
        floatingChildren.add(child)
    }

    /**
     * * Checks whether the specified component is a child of this component
     */
    override fun hasChild(child: UIElement): Boolean = children.contains(child)

    /**
     * * Checks whether this component is a child of the specified [parent] component
     */
    override fun hasParent(parent: UIElement?): Boolean {
        val comp = parent ?: this.parent
        if (comp == null) return false
        return comp == this.parent
    }

    /**
     * * Sets this component as a child of the specified [parent] component
     */
    override fun setChildOf(parent: UIElement) = apply {
        if (parent.hasChild(this)) return@apply
        parent.addChild(this)
    }

    /**
     * * Removes the specified child from this component
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    override fun removeChild(child: UIElement): Boolean {
        val removed = children.remove(child)
        markDirty()
        return removed
    }

    /**
     * * Removes this component from its parent
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    override fun remove(): Boolean = parent?.removeChild(this) ?: false

    /**
     * * Clears all the children from this component
     * * Removes this component as their parent as well as marking them dirty
     */
    override fun clearChildren() = apply {
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
    override fun setPos(x: Double, y: Double) = apply {
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
    override fun setPosition(x: Double, y: Double) = setPos(x, y)

    /**
     * * Sets the size for this component
     * * Note: this marks the component as dirty
     * @param width The Width for this component in percent (`0-100`)
     * @param height The Height for this component in percent (`0-100`)
     */
    override fun setSize(width: Double, height: Double) = apply {
        _width = width
        _height = height
        markDirty()
    }

    /**
     * * Checks whether the specified [x] and [y] are in the bounds of this component
     * * Note: if the component's bounds have not yet been set it will return `false`
     */
    override fun inBounds(x: Double, y: Double): Boolean {
        if (bounds.x1 == -1.0) return false
        return x in bounds.x1..bounds.x2 && y in bounds.y1..bounds.y2
    }

    /**
     * * Checks whether the specified [UIMouseEvent] is in the bounds of this component
     */
    override fun inBounds(event: UIMouseEvent): Boolean = inBounds(event.x, event.y)

    /**
     * * Adds a single [UIEffect] to this component
     */
    override fun addEffect(effect: UIEffect) = apply {
        effect.component = this
        effects.add(effect)
    }

    /**
     * * Adds multiple [UIEffect] to this component
     */
    override fun addEffects(vararg effects: UIEffect) = apply {
        effects.forEach { it.component = this }
        this.effects.addAll(effects)
    }

    /**
     * * Removes the specified [effect] from this component
     */
    override fun removeEffect(effect: UIEffect): Boolean = effects.remove(effect)

    /**
     * * Removes the [UIEffect]s that are instance of the specified [clazz]
     */
    override fun <T: UIEffect> removeEffects(clazz: Class<T>): Boolean = effects.removeIf { clazz.isInstance(it) }

    /**
     * * Checks whether this component's [focused] variable is true or false
     */
    override fun hasFocus(): Boolean = focused

    /**
     * * Sets the [xAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    override fun setXAnimation(name: String, maxTime: Float) = apply {
        xAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [yAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    override fun setYAnimation(name: String, maxTime: Float) = apply {
        yAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [widthAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    override fun setWidthAnimation(name: String, maxTime: Float) = apply {
        widthAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Sets the [heightAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    override fun setHeightAnimation(name: String, maxTime: Float) = apply {
        heightAnimation = Animation(Animations.getParameterByName(name), maxTime)
    }

    /**
     * * Checks whether this [UIElement] component is dirty
     */
    override fun isDirty(): Boolean = isSelfDirty || (isChildDirty && isDynamic())

    /**
     * * Replaces the specified child with a new one
     * @returns a boolean that specifies whether the component was successfully replaced or not
     */
    override fun replaceChild(newComp: UIElement, oldComp: UIElement): Boolean {
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
    override fun insertChild(comp: UIElement, idx: Int): Boolean {
        if (idx < 0 || idx > children.size) return false

        comp.parent = this
        children.add(idx, comp)
        return true
    }

    /**
     * * Sets the [hidden] variable to `true`
     */
    override fun hide() = apply {
        hidden = true
        recalculateConstraint()
    }

    /**
     * * Sets the [hidden] variable to `false`
     */
    override fun unhide() = apply {
        hidden = false
        recalculateConstraint()
    }

    override fun recalculateConstraint() {
        if (
            xConstraint == null &&
            yConstraint == null &&
            widthConstraint == null &&
            heightConstraint == null
        ) return

        val idx = parent?.children?.indexOf(this) ?: return
        if (idx == -1) return
        val children = parent!!.children

        for (jdx in idx until children.size - 1) {
            val child = children[jdx]
            if (
                child.xConstraint != null ||
                child.yConstraint != null ||
                child.widthConstraint != null ||
                child.heightConstraint != null
            ) child.markDirty()
        }
    }

    /**
     * * Checks whether this component is the main component
     * * Usually the main component is the one that is at the top of the hierarchy
     * and thus has no parent, therefore we can do single calculations here and
     * pass them through to the children so its only done once and not per child
     */
    override fun isMainComponent(): Boolean = parent == null

    /**
     * * Gets the component that is located at the specified `x` and `y`
     * * If no component is found it will return `null`
     */
    override fun getComponentAt(x: Double, y: Double): UIElement? {
        var comp: UIElement? = null
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
    override fun unfocus() {
        propagateUnfocus(UIFocusEvent(-1.0, -1.0, false, this))
    }

    /**
     * * Adds a layout to handle the drawing of children
     */
    override fun addLayout(layout: Layout) = apply {
        layout.parent = this
        this.layout = layout
    }

    /**
     * * Removes the current layout
     */
    override fun removeLayout() = apply {
        layout = null
    }

    fun isDynamic() =
        xConstraint != null ||
        yConstraint != null ||
        widthConstraint != null ||
        heightConstraint != null

    override fun getLayoutElement(): UIElement? {
        return if (isDynamic()) parent?.getLayoutElement()
            else this
    }

    override fun updateFixed(finalize: Boolean) = apply {
        if (isDynamic()) return@apply

        val p = if (finalize) parent else parent?.getLayoutElement()

        val parentX = p?.x ?: 0.0
        val parentY = p?.y ?: 0.0
        val parentWidth = p?.width ?: scaledResolution?.scaledWidth_double ?: 0.0
        val parentHeight = p?.height ?: scaledResolution?.scaledHeight_double ?: 0.0

        x = _x / 100 * parentWidth + parentX
        y = _y / 100 * parentHeight + parentY
        width = _width / 100 * parentWidth
        height = _height / 100 * parentHeight
        bounds = UIElement.Boundaries(x, y, x + width, y + height)
    }

    override fun updateDynamic() = apply {
        if (!isDynamic()) return@apply

        val p = parent?.getLayoutElement()

        val parentX = p?.x ?: 0.0
        val parentY = p?.y ?: 0.0
        val parentWidth = p?.width ?: scaledResolution?.scaledWidth_double ?: 0.0
        val parentHeight = p?.height ?: scaledResolution?.scaledHeight_double ?: 0.0

        x = xConstraint?.x(this) ?: (_x / 100 * parentWidth + parentX)
        y = yConstraint?.y(this) ?: (_y / 100 * parentHeight + parentY)
        width = widthConstraint?.width(this) ?: (_width / 100 * parentWidth)
        height = heightConstraint?.height(this) ?: (_height / 100 * parentHeight)
        bounds = UIElement.Boundaries(x, y, x + width, y + height)
    }

    override fun updateLayout() = apply {
        onUpdate()
        hookUpdate?.invoke()
        layout?.onUpdate()
        xConstraint?.onUpdate(this)
        yConstraint?.onUpdate(this)
        widthConstraint?.onUpdate(this)
        heightConstraint?.onUpdate(this)
    }

    fun calculateLayout() {
        val q = ArrayDeque<UIElement>()
        val sizeQ = ArrayDeque<UIElement>()

        q.add(this)
        sizeQ.add(this)
        while (true) {
            val e = q.removeFirstOrNull() ?: break

            e.isSelfDirty = false
            e.isChildDirty = false

            e.children.forEach {
                if (!it.isDirty()) return@forEach

                q.add(it)
                sizeQ.add(it)

                it.updateFixed(false)
            }
        }

        val layoutQ = ArrayDeque(sizeQ)
        val finalizeQ = ArrayDeque(sizeQ)

        while (true) {
            val e = sizeQ.removeLastOrNull() ?: break

            e.updateDynamic()
        }

        while (true) {
            val e = layoutQ.removeFirstOrNull() ?: break

            e.updateLayout()
        }

        while (true) {
            val e = finalizeQ.removeLastOrNull() ?: break

            e.updateFixed(true)
        }
    }

    override fun checkUpdate() = apply {
        if (isDynamic()) {
            if (isChildDirty || isSelfDirty) calculateLayout()
        } else if (isSelfDirty) {
            isSelfDirty = false
            updateFixed(false)
            updateLayout()
        }
    }

    /**
     * * Override this method if you need to do something **before** the component is drawn
     */
    override fun preDraw(x2: Double, y2: Double) {}
    /**
     * * Override this method if you need to do something **after** the component is drawn
     */
    override fun postDraw() {}
    /**
     * * Override this method if you need to do something **before** the children are drawn
     */
    override fun preChildDraw() {}
    /**
     * * Override this method if you need to do something **after** the children are drawn
     */
    override fun postChildDraw() {}
    /**
     * * Override this method to draw your custom component
     */
    override fun render() {}

    override fun drawChildren(x2: Double, y2: Double) {
        children.toList().forEach {
            if (it is FloatingUI) return@forEach
            it.draw(x2, y2)
        }
    }

    override fun draw(x2: Double, y2: Double) {
        // Check the scaledResolution
        if (isMainComponent()) {
            val sr = ScaledResolution(Minecraft.getInstance())
            if (scaledResolution == null) {
                scaledResolution = sr
                propagateResize(this, sr)
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

        if (isMainComponent() && RenderSystem.tryGetDevice()?.deviceInfo?.backendName()?.lowercase() == "opengl") {
            GlStateManager._enableBlend()
            GlStateManager._disableCull()
        }

        try {
            // Handle mouse inputs if the component does not have a parent
            // this _should_ mean that the component is at the top of the hierarchy
            // so only this component needs to handle the inputs and pass them through
            if (isMainComponent()) handleMouseInput()
            checkUpdate()
            effects.forEach { it.preDraw(x2, y2) }
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
            floatingChildren.forEach { it.updateFloating() }
            x += x2
            y += y2
            layout?.preChildDraw()
            effects.forEach { it.preChildDraw() }
            preChildDraw()
            drawChildren(x2, y2)
            if (isMainComponent()) {
                floatingChildren.forEach { it.draw(x2, y2) }
            }
            layout?.postChildDraw()
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
                if (RenderSystem.tryGetDevice()?.deviceInfo?.backendName()?.lowercase() == "opengl") {
                    GlStateManager._disableBlend()
                    GlStateManager._enableCull()
                }
                ScissorEffect.disableScissor()
            }
        }
    }

    /**
     * * Call this method inside a Screen's Screen.keyPressed
     * this will handle all the keytyped as well as only trigger if it's the highest component
     * in the hierarchy
     */
    override fun handleKeyInput(keycode: Int, scanCode: Int) {
        if (parent != null || !focused) return
        val keyName = GLFW.glfwGetKeyName(keycode, scanCode)
        val char = keyName?.single()
        propagateKeyTyped(UIKeyType(keycode, char, char.toString(), this))
    }

    override fun handleCharType(codepoint: Int, codeStr: String, modifiers: Int) {
        if (parent != null || !focused) return
        propagatgeCharTyped(UICharEvent(codepoint, codeStr, modifiers, this))
    }

    override fun handleMouseInput() {
        if (scaledResolution == null) return

        val mxd = Renderer.getMouseX(scaledResolution!!)
        val myd = Renderer.getMouseY(scaledResolution!!)
        val insideBounds = inBounds(mxd, myd)

        // Handle scroll
        if (!hasScrollListener) {
            MouseState.onMouseScroll { mx, my, delta ->
                val inComp = inBounds(mx, my)
                // scroll should never be 0 but just in case
                if (delta == 0 || !inComp) return@onMouseScroll
                propagateMouseScroll(UIScrollEvent(mx, my, delta, this))
            }
            hasScrollListener = true
        }

        // Handle mouseEnter/Hover/Leave
        val mouseEvent = UIMouseEvent(mxd, myd, this)
        if (insideBounds) {
            propagateMouseEnter(mouseEvent)
            propagateMouseHover(mouseEvent)
        }
        propagateMouseLeave(mouseEvent)

        mouseInBounds = insideBounds

        // Handle mouse click/release/drag
        for (btn in 0..8) {
            val oldState = mouseState[btn] ?: false
            val btnState = MouseState.isButtonDown(btn)
            if (oldState != btnState) {
                mouseState[btn] = btnState

                val clickEvent = UIClickEvent(mxd, myd, btn, this)
                val focusEvent = UIFocusEvent(mxd, myd, false, this)

                if (insideBounds) {
                    if (oldState) propagateMouseRelease(clickEvent)
                    else {
                        propagateMouseClick(clickEvent)
                        propagateFocus(UIFocusEvent(mxd, myd, true, this))
                    }
                }
                if (focused && !insideBounds) {
                    propagateUnfocus(focusEvent)
                } else if (!oldState) {
                    for (child in children) {
                        child.propagateUnfocus(focusEvent)
                        if (!focusEvent.propagate) break
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

    override fun <T : UIMouseEvent> modifyChildMouseEvent(event: T) {}
    override fun <T : UIMouseEvent> resetChildMouseEvent(event: T) {}

    override fun propagateMouseScroll(event: UIScrollEvent) {
        onMouseScroll(event)
        hookMouseScroll?.invoke(event)
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateMouseScroll(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseClick(event: UIClickEvent) {
        onMouseClick(event)
        hookMouseClick?.invoke(event)
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateMouseClick(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseRelease(event: UIClickEvent) {
        onMouseRelease(event)
        hookMouseRelease?.invoke(event)
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateMouseRelease(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseEnter(event: UIMouseEvent) {
        if (!mouseInBounds) {
            onMouseEnter(event)
            hookMouseEnter?.invoke(event)
            mouseInBounds = true
        }
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateMouseEnter(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseLeave(event: UIMouseEvent) {
        if (mouseInBounds && !inBounds(event)) {
            onMouseLeave(event)
            hookMouseLeave?.invoke(event)
            mouseInBounds = false
        }
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (child.hidden) continue
            child.propagateMouseLeave(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseHover(event: UIMouseEvent) {
        onMouseHover(event)
        hookMouseHover?.invoke(event)
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateMouseHover(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateMouseDrag(event: UIDragEvent) {
        onMouseDrag(event)
        hookMouseDrag?.invoke(event)
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (child.hidden) continue
            child.onMouseDragOut(event)
            if (!child.inBounds(event)) continue

            child.propagateMouseDrag(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateFocus(event: UIFocusEvent) {
        if (focused != event.state) {
            focused = true
            onFocus(event)
            hookFocus?.invoke(event)
            focused = event.state
        }
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (!child.inBounds(event) || child.hidden) continue

            child.propagateFocus(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateUnfocus(event: UIFocusEvent) {
        if (focused && !inBounds(event)) {
            focused = false
            onUnfocus(event)
            onLostFocus(event)
            hookUnfocus?.invoke(event)
        }
        if (!event.propagate) return

        modifyChildMouseEvent(event)
        onPreChildPropagate(event)
        preChildPropagate?.let { it(event) }

        for (child in children.toList()) {
            if (child.hidden) continue
            child.propagateUnfocus(event)
            if (!event.propagate) break
        }

        onPostChildPropagation(event)
        postChildPropagate?.let { it(event) }
        resetChildMouseEvent(event)
    }

    override fun propagateKeyTyped(event: UIKeyType) {
        onKeyTyped(event)
        onKeyType(event)
        hookKeyType?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.focused || child.hidden) continue

            child.propagateKeyTyped(event)
            if (!event.propagate) break
        }
    }

    override fun propagatgeCharTyped(event: UICharEvent) {
        onCharTyped(event)
        onCharType(event)
        hookCharType?.invoke(event)
        if (!event.propagate) return

        for (child in children.toList()) {
            if (!child.focused || child.hidden) continue

            child.propagatgeCharTyped(event)
            if (!event.propagate) break
        }
    }

    override fun propagateResize(comp: UIElement, scaledResolution: ScaledResolution) {
        markDirty()
        onResize(comp, scaledResolution)
        hookResize?.invoke(comp, scaledResolution)

        for (child in children.toList()) child.propagateResize(comp, scaledResolution)
    }

    override fun propagateError(trace: Array<out StackTraceElement>) {
        onError(trace)
        hookError?.invoke(trace)

        for (child in children.toList()) child.propagateError(trace)
    }

    override fun onResize(comp: UIElement, scaledResolution: ScaledResolution) = apply {}
    open fun onResize(cb: (comp: UIElement, scaledResolution: ScaledResolution) -> Unit) = apply {
        hookResize = cb
    }
    override fun onError(trace: Array<out StackTraceElement>) = apply {}
    open fun onError(cb: (trace: Array<out StackTraceElement>) -> Unit) = apply {
        hookError = cb
    }

    override fun onMouseClick(event: UIClickEvent) = apply {}
    open fun onMouseClick(cb: (event: UIClickEvent) -> Unit) = apply {
        hookMouseClick = cb
    }
    override fun onMouseDrag(event: UIDragEvent) = apply {}
    open fun onMouseDrag(cb: (event: UIDragEvent) -> Unit) = apply {
        hookMouseDrag = cb
    }
    /**
     * * Triggers whenever the mouse is dragged inside the parent component
     * but the drag was started inside `this` component
     */
    override fun onMouseDragOut(event: UIDragEvent) = apply {}
    override fun onMouseRelease(event: UIClickEvent) = apply {}
    open fun onMouseRelease(cb: (event: UIClickEvent) -> Unit) = apply {
        hookMouseRelease = cb
    }
    override fun onMouseEnter(event: UIMouseEvent) = apply {}
    open fun onMouseEnter(cb: (event: UIMouseEvent) -> Unit) = apply {
        hookMouseEnter = cb
    }
    override fun onMouseHover(event: UIMouseEvent) = apply {}
    open fun onMouseHover(cb: (event: UIMouseEvent) -> Unit) = apply {
        hookMouseHover = cb
    }
    override fun onMouseLeave(event: UIMouseEvent) = apply {}
    open fun onMouseLeave(cb: (event: UIMouseEvent) -> Unit) = apply {
        hookMouseLeave = cb
    }
    override fun onMouseScroll(event: UIScrollEvent) = apply {}
    open fun onMouseScroll(cb: (event: UIScrollEvent) -> Unit) = apply {
        hookMouseScroll = cb
    }
    override fun onFocus(event: UIFocusEvent) = apply {}
    open fun onFocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hookFocus = cb
    }
    override fun onUnfocus(event: UIFocusEvent) = apply {}
    open fun onUnfocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hookUnfocus = cb
    }
    override fun onLostFocus(event: UIFocusEvent) = apply {}
    open fun onLostFocus(cb: (event: UIFocusEvent) -> Unit) = apply {
        hookUnfocus = cb
    }

    override fun onKeyType(event: UIKeyType) = apply {}
    open fun onKeyType(cb: (event: UIKeyType) -> Unit) = apply {
        hookKeyType = cb
    }
    override fun onKeyTyped(event: UIKeyType) = apply {}
    open fun onKeyTyped(cb: (event: UIKeyType) -> Unit) = apply {
        hookKeyType = cb
    }

    override fun onCharType(event: UICharEvent) = apply {}
    open fun onCharType(cb: (UICharEvent) -> Unit) = apply {
        hookCharType = cb
    }

    override fun onCharTyped(event: UICharEvent) = apply {}
    open fun onCharTyped(cb: (UICharEvent) -> Unit) {
        hookCharType = cb
    }

    override fun onUpdate() = apply {}
    open fun onUpdate(cb: () -> Unit) = apply {
        hookUpdate = cb
    }

    override fun onPreChildPropagate(event: UIMouseEvent) = apply {}
    open fun onPreChildPropagate(cb: (event: UIMouseEvent) -> Unit) = apply {
        preChildPropagate = cb
    }

    override fun onPostChildPropagation(event: UIMouseEvent) = apply {}
    open fun onPostChildPropagation(cb: (event: UIMouseEvent) -> Unit) = apply {
        postChildPropagate = cb
    }

    data class State(var x: Double, var y: Double)
}
