package com.github.synnerz.talium.components

abstract class FloatingUI(val origParent: UIElement) : UIBase(0.0, 0.0, 0.0, 0.0) {
    private var setParent = false

    override fun update() = apply {}

    abstract fun setFloatingPos(parent: UIElement)

    override fun draw(x2: Double, y2: Double) {
        if (!origParent.hidden) super.draw(x2, y2)
    }

    fun updateFloating() {
        if (!setParent) {
            var curr = origParent
            while (curr.parent != null) curr = curr.parent!!

            curr.addFloatingChild(this)
            curr.addChild(this)

            setParent = true
        }

        if (hidden) return

        setFloatingPos(origParent)
        bounds = UIElement.Boundaries(x, y, x + width, y + height)
        onUpdate()
        hookUpdate?.invoke()
        layout?.onUpdate()

        markDirty()
        isSelfDirty = false
    }

    init {
        origParent.addFloatingChild(this)
    }
}