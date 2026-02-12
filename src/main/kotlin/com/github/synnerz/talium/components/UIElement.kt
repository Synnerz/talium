package com.github.synnerz.talium.components

import com.github.synnerz.talium.animations.Animation
import com.github.synnerz.talium.constraints.UIHeightConstraint
import com.github.synnerz.talium.constraints.UIWidthConstraint
import com.github.synnerz.talium.constraints.UIXConstraint
import com.github.synnerz.talium.constraints.UIYConstraint
import com.github.synnerz.talium.effects.UIEffect
import com.github.synnerz.talium.events.*
import com.github.synnerz.talium.layout.Layout
import com.github.synnerz.talium.utils.ScaledResolution
import java.awt.Color

interface UIElement {
    var _x: Double
    var _y: Double
    var _width: Double
    var _height: Double
    var parent: UIElement?
    val children: MutableList<UIElement>
    val floatingChildren: MutableList<FloatingUI>

    /**
     * * These are listeners made by the user
     * * i.e. if i want to listen for a mouseClick on a component, i'll add a click hook
     */
    var hookMouseScroll: ((event: UIScrollEvent) -> Unit)?
    var hookMouseClick: ((event: UIClickEvent) -> Unit)?
    var hookMouseRelease: ((event: UIClickEvent) -> Unit)?
    var hookMouseEnter: ((event: UIMouseEvent) -> Unit)?
    var hookMouseHover: ((event: UIMouseEvent) -> Unit)?
    var hookMouseLeave: ((event: UIMouseEvent) -> Unit)?
    var hookMouseDrag: ((event: UIDragEvent) -> Unit)?
    var hookFocus: ((event: UIFocusEvent) -> Unit)?
    var hookUnfocus: ((event: UIFocusEvent) -> Unit)?
    var hookKeyType: ((event: UIKeyType) -> Unit)?
    var hookCharType: ((event: UICharEvent) -> Unit)?
    var hookResize: ((comp: UIElement, scaledResolution: ScaledResolution) -> Unit)?
    var hookError: ((trace: Array<out StackTraceElement>) -> Unit)?
    var hookUpdate: (() -> Unit)?

    /** * Runs before the event is passed through to the children of this component */
    var preChildPropagate: ((event: UIMouseEvent) -> Unit)?

    /** * Runs after the event is passed through to the children of this component */
    var postChildPropagate: ((event: UIMouseEvent) -> Unit)?
    var mouseInBounds: Boolean

    /**
     * * Used internally to scale the position and size of the component
     * as well as to trigger the [onResize] hook/listener
     */
    var scaledResolution: ScaledResolution?

    /** * Note: if you call the setter it will not mark the component as dirty */
    var x: Double

    /** * Note: if you call the setter it will not mark the component as dirty */
    var y: Double

    /** * Note: if you call the setter it will not mark the component as dirty */
    var width: Double

    /** * Note: if you call the setter it will not mark the component as dirty */
    var height: Double
    var bounds: Boundaries
    var bgColor: Color

    /** * Variable that lets the component be known if its focused or not, mostly used for keyboard inputs */
    var focused: Boolean

    /** * _Should_ be used whenever the component goes left-right or right-left */
    var xAnimation: Animation?

    /** * _Should_ be used whenever the component goes top-bottom or bottom-top */
    var yAnimation: Animation?

    /** * _Should_ be used whenever the component grows or shrinks in width */
    var widthAnimation: Animation?

    /** * _Should_ be used whenever the component grows or shrinks in height */
    var heightAnimation: Animation?

    /** * Whether this component is hidden or not */
    var hidden: Boolean

    /** * The layout to use for the children drawing */
    var layout: Layout?

    /** * The X Constraint for this component */
    var xConstraint: UIXConstraint?

    /** * The Y Constraint for this component */
    var yConstraint: UIYConstraint?

    /** * The Width Constraint for this component */
    var widthConstraint: UIWidthConstraint?

    /** * The Height Constraint for this component */
    var heightConstraint: UIHeightConstraint?

    /**
     * * Field to check whether this component is dirty or not
     * * When a component is marked as dirty this means that
     * they need to be updated in the size, position and as well as their children's size and position
     * * i.e. if the window is resized this _should_ be marked as dirty, so it can recalculate the position etc
     */
    var isSelfDirty: Boolean
    var isChildDirty: Boolean

    /**
     * * This class represents the current boundaries of the component
     * @param x1 X position in pixels
     * @param y1 Y position in pixels
     * @param x2 X + Width position in pixels
     * @param y2 Y + Height position in pixels
     */
    data class Boundaries(val x1: Double, val y1: Double, val x2: Double, val y2: Double)

    /**
     * * Sets the [dirty] variable of this component to the specified state
     */
    fun setDirty(state: Boolean = true): UIElement

    /**
     * * Marks this component as dirty, so it can recalculate positions next render
     */
    fun markDirty(): UIElement

    fun markChildDirty(): UIElement

    /**
     * * Sets the color of this component
     * * Note: if there is a color effect it will override this color
     */
    fun setColor(color: Color): UIElement

    /**
     * * Adds the specified [child] to this component
     * * Note: if the component already has a parent it will be removed and re-assigned to this one
     */
    fun addChild(child: UIElement): UIElement

    fun addFloatingChild(child: FloatingUI): UIElement

    /**
     * * Checks whether the specified component is a child of this component
     */
    fun hasChild(child: UIElement): Boolean

    /**
     * * Checks whether this component is a child of the specified [parent] component
     */
    fun hasParent(parent: UIElement? = null): Boolean

    /**
     * * Sets this component as a child of the specified [parent] component
     */
    fun setChildOf(parent: UIElement): UIElement

    /**
     * * Removes the specified child from this component
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    fun removeChild(child: UIElement): Boolean

    /**
     * * Removes this component from its parent
     * * @returns a boolean that signifies whether the component was successfully removed or not
     */
    fun remove(): Boolean

    /**
     * * Clears all the children from this component
     * * Removes this component as their parent as well as marking them dirty
     */
    fun clearChildren(): UIElement

    /**
     * * Sets the position for this component
     * * Note: this marks the component as dirty
     * @param x The X position in percent (`0-100`)
     * @param y The Y position in percent (`0-100`)
     */
    fun setPos(x: Double, y: Double): UIElement

    /**
     * * Sets the position for this component
     * * Note: this marks the component as dirty
     * @param x The X position in percent (`0-100`)
     * @param y The Y position in percent (`0-100`)
     */
    fun setPosition(x: Double, y: Double): UIElement

    /**
     * * Sets the size for this component
     * * Note: this marks the component as dirty
     * @param width The Width for this component in percent (`0-100`)
     * @param height The Height for this component in percent (`0-100`)
     */
    fun setSize(width: Double, height: Double): UIElement

    /**
     * * Checks whether the specified [x] and [y] are in the bounds of this component
     * * Note: if the component's bounds have not yet been set it will return `false`
     */
    fun inBounds(x: Double, y: Double): Boolean

    /**
     * * Checks whether the specified [UIMouseEvent] is in the bounds of this component
     */
    fun inBounds(event: UIMouseEvent): Boolean = inBounds(event.x, event.y)

    /**
     * * Adds a single [UIEffect] to this component
     */
    fun addEffect(effect: UIEffect): UIElement

    /**
     * * Adds multiple [UIEffect] to this component
     */
    fun addEffects(vararg effects: UIEffect): UIElement

    /**
     * * Removes the specified [effect] from this component
     */
    fun removeEffect(effect: UIEffect): Boolean

    /**
     * * Removes the [UIEffect]s that are instance of the specified [clazz]
     */
    fun <T: UIEffect> removeEffects(clazz: Class<T>): Boolean

    /**
     * * Checks whether this component's [focused] variable is true or false
     */
    fun hasFocus(): Boolean

    /**
     * * Sets the [xAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    fun setXAnimation(name: String, maxTime: Float = 500f): UIElement

    /**
     * * Sets the [yAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    fun setYAnimation(name: String, maxTime: Float = 500f): UIElement

    /**
     * * Sets the [widthAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    fun setWidthAnimation(name: String, maxTime: Float = 500f): UIElement

    /**
     * * Sets the [heightAnimation] to the given [Animation] [name]
     * * Note: If the animation was not found it will throw an [IllegalArgumentException]
     * @param maxTime Maximum time the animation should last for in milliseconds
     */
    fun setHeightAnimation(name: String, maxTime: Float = 500f): UIElement

    /**
     * * Checks whether this [UIElement] component is dirty
     */
    fun isDirty(): Boolean

    /**
     * * Replaces the specified child with a new one
     * @returns a boolean that specifies whether the component was successfully replaced or not
     */
    fun replaceChild(newComp: UIElement, oldComp: UIElement): Boolean

    /**
     * * Inserts the specified child into the specified index
     * @returns a boolean that specifies whether the component was successfully inserted or not
     */
    fun insertChild(comp: UIElement, idx: Int): Boolean

    /**
     * * Sets the [hidden] variable to `true`
     */
    fun hide(): UIElement

    /**
     * * Sets the [hidden] variable to `false`
     */
    fun unhide(): UIElement

    /** * Called whenever a constraint _should_ be updated, for example whenever calling [hide] */
    fun recalculateConstraint()

    /**
     * * Checks whether this component is the main component
     * * Usually the main component is the one that is at the top of the hierarchy
     * and thus has no parent, therefore we can do single calculations here and
     * pass them through to the children so its only done once and not per child
     */
    fun isMainComponent(): Boolean

    /**
     * * Gets the component that is located at the specified `x` and `y`
     * * If no component is found it will return `null`
     */
    fun getComponentAt(x: Double, y: Double): UIElement?

    /**
     * * Un-focuses the component
     */
    fun unfocus()

    /**
     * * Adds a layout to handle the drawing of children
     */
    fun addLayout(layout: Layout): UIElement

    /**
     * * Removes the current layout
     */
    fun removeLayout(): UIElement

    fun getLayoutElement(): UIElement?

    fun updateFixed(): UIElement

    fun updateDynamic(): UIElement

    fun updateLayout(): UIElement

    fun checkUpdate(): UIElement

    /**
     * * Override this method if you need to do something **before** the component is drawn
     */
    fun preDraw(x2: Double = 0.0, y2: Double = 0.0)

    /**
     * * Override this method if you need to do something **after** the component is drawn
     */
    fun postDraw()

    /**
     * * Override this method if you need to do something **before** the children are drawn
     */
    fun preChildDraw()

    /**
     * * Override this method if you need to do something **after** the children are drawn
     */
    fun postChildDraw()

    /**
     * * Override this method to draw your custom component
     */
    fun render()

    fun drawChildren(x2: Double = 0.0, y2: Double = 0.0)

    fun draw(x2: Double = 0.0, y2: Double = 0.0)

    /**
     * * Call this method inside a Screen's Screen.keyPressed
     * this will handle all the keytyped as well as only trigger if it's the highest component
     * in the hierarchy
     */
    fun handleKeyInput(keycode: Int, scanCode: Int)
    fun handleCharType(codepoint: Int, codeStr: String, modifiers: Int)
    fun handleMouseInput()
    fun <T : UIMouseEvent> modifyChildMouseEvent(event: T)
    fun <T : UIMouseEvent> resetChildMouseEvent(event: T)
    fun propagateMouseScroll(event: UIScrollEvent)
    fun propagateMouseClick(event: UIClickEvent)
    fun propagateMouseRelease(event: UIClickEvent)
    fun propagateMouseEnter(event: UIMouseEvent)
    fun propagateMouseLeave(event: UIMouseEvent)
    fun propagateMouseHover(event: UIMouseEvent)
    fun propagateMouseDrag(event: UIDragEvent)
    fun propagateFocus(event: UIFocusEvent)
    fun propagateUnfocus(event: UIFocusEvent)
    fun propagateKeyTyped(event: UIKeyType)
    fun propagatgeCharTyped(event: UICharEvent)
    fun propagateResize(comp: UIElement, scaledResolution: ScaledResolution)
    fun propagateError(trace: Array<out StackTraceElement>)
    fun onResize(comp: UIElement, scaledResolution: ScaledResolution): UIElement
    fun onError(trace: Array<out StackTraceElement>): UIElement
    fun onMouseClick(event: UIClickEvent): UIElement
    fun onMouseDrag(event: UIDragEvent): UIElement

    /**
     * * Triggers whenever the mouse is dragged inside the parent component
     * but the drag was started inside `this` component
     */
    fun onMouseDragOut(event: UIDragEvent): UIElement
    fun onMouseRelease(event: UIClickEvent): UIElement
    fun onMouseEnter(event: UIMouseEvent): UIElement
    fun onMouseHover(event: UIMouseEvent): UIElement
    fun onMouseLeave(event: UIMouseEvent): UIElement
    fun onMouseScroll(event: UIScrollEvent): UIElement
    fun onFocus(event: UIFocusEvent): UIElement
    fun onUnfocus(event: UIFocusEvent): UIElement
    fun onLostFocus(event: UIFocusEvent): UIElement
    fun onKeyType(event: UIKeyType): UIElement
    fun onKeyTyped(event: UIKeyType): UIElement
    fun onCharType(event: UICharEvent): UIElement
    fun onCharTyped(event: UICharEvent): UIElement
    fun onUpdate(): UIElement
    fun onPreChildPropagate(event: UIMouseEvent): UIElement
    fun onPostChildPropagation(event: UIMouseEvent): UIElement
}