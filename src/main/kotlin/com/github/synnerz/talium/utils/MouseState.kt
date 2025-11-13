package com.github.synnerz.talium.utils

import org.lwjgl.glfw.GLFW

object MouseState {
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
    var dWheel = 0

    fun isButtonDown(btn: Int) = buttonsDown[btn] == GLFW.GLFW_PRESS
}