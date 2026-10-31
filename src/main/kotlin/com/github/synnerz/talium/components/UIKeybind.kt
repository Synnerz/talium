package com.github.synnerz.talium.components

import com.github.synnerz.talium.events.UIClickEvent
import com.github.synnerz.talium.events.UIFocusEvent
import com.github.synnerz.talium.events.UIKeyType
import com.mojang.blaze3d.platform.InputConstants
import kotlin.math.abs

open class UIKeybind @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var keycode: Int = -1,
    parent: UIElement? = null,
) : UIRect(_x, _y, _width, _height, parent = parent) {
    private var textCodeDirty = false
    open val keyNameText by lazy {
        object : UIWrappedText(
            0.0, 0.0,
            100.0, 100.0,
            "UNKNOWN",
            true,
            this@UIKeybind,
        ) {
            override fun render() {
                if (textCodeDirty) text = forName(this@UIKeybind.keycode)
                textCodeDirty = false

                super.render()
            }
        }
    }
    private var toggled = false

    open fun setBind(keycode: Int) {
        this.keycode = keycode
    }

    open fun updateKeyName() {
        textCodeDirty = true
    }

    open fun isMouseButton(): Boolean = keycode < -1

    override fun onUpdate() = apply {
        updateKeyName()
    }

    override fun onFocus(event: UIFocusEvent) = apply {
        keyNameText.text = "****"
    }

    override fun onMouseRelease(event: UIClickEvent) = apply {
        if (!focused) return@apply
        if (!toggled) {
            toggled = true
            return@apply
        }

        setBind(-100 + event.button)
        updateKeyName()
        unfocus()
        toggled = false
    }

    override fun onKeyType(event: UIKeyType) = apply {
        if (!focused) return@apply
        toggled = false
        // TODO: note you have to handle cancelling this key
        //  call [unfocusKeybinds] on the main parent
        if (event.keycode == InputConstants.KEY_ESCAPE) {
            setBind(-1)
            updateKeyName()
            return@apply
        }

        setBind(event.keycode)
        updateKeyName()
        unfocus()
    }

    companion object {
        /**
         * * Attempts to resolve the name of the current keybind
         */
        fun forName(keycode: Int): String {
            if (keycode < -1) return "M${abs(-100 % keycode)}"
            if (keycode == -1) return "UNKNOWN"

            val name = InputConstants.Type.KEYBOARD.getOrCreate(keycode).displayName.string
            return "KEY ${name.replace("Keypad ", "KP")}"
        }
    }
}