package com.github.synnerz.talium.animations

import kotlin.math.max

open class Animation @JvmOverloads constructor(
    val animation: IAnimation,
    var maxTime: Float = 500f
) {
    var shouldAnimate: Boolean = false
    var animationStart: Long = 0L

    open fun start() = apply {
        animationStart = System.currentTimeMillis()
        shouldAnimate = true
    }

    open fun stop() = apply {
        animationStart = 0L
        shouldAnimate = false
    }

    open fun render() {
        if (!shouldAnimate) return
        if (System.currentTimeMillis() - animationStart >= maxTime) {
            shouldAnimate = false
            animationStart = 0L
            return
        }
    }

    open fun getEase(): Float {
        val time = System.currentTimeMillis() - animationStart
        return max(animation.getEase(time.toFloat() / maxTime), 0f)
    }
}
