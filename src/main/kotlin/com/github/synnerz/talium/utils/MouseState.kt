package com.github.synnerz.talium.utils

import org.lwjgl.glfw.GLFW

object MouseState {
    private val scrollListeners = mutableListOf<(Double, Double, Int) -> Unit>()
    val buttonsDown = mutableMapOf(
        0 to 0,
        1 to 0,
        2 to 0,
        3 to 0,
        4 to 0,
        5 to 0,
        6 to 0,
        7 to 0
    )

    fun isButtonDown(btn: Int) = buttonsDown[btn] == GLFW.GLFW_PRESS

    fun triggerMouseScroll(mx: Double, my: Double, delta: Int) {
        for (cb in scrollListeners)
            cb(mx, my, delta)
    }

    fun onMouseScroll(cb: (Double, Double, Int) -> Unit) {
        scrollListeners.add(cb)
    }
}