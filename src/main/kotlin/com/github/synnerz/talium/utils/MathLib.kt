package com.github.synnerz.talium.utils

import kotlin.math.*

object MathLib {
    val PI2 = PI * 2

    fun rescale(n: Float, oldMin: Double, oldMax: Double, newMin: Double, newMax: Double): Double {
        return (n - oldMin) / (oldMax - oldMin) * (newMax - newMin) + newMin
    }

    fun getPowIn(elapsedTime: Float, pow: Double): Float {
        return elapsedTime.pow(pow.toFloat())
    }

    fun getPowOut(elapsedTime: Float, pow: Double): Float {
        return (1.0 - (1.0 - elapsedTime.toDouble()).pow(pow)).toFloat()
    }

    fun getPowInOut(elapsedTime: Float, pow: Double): Float {
        val v = elapsedTime * 2
        return if (v < 1) {
            0.5f * v.pow(pow.toFloat())
        } else {
            0.5f * (2 - (2 - v).pow(pow.toFloat()))
        }
    }

    fun getBackInOut(elapsedTime: Float, amount: Float): Float {
        var v = elapsedTime * 2f
        var n = amount
        n *= 1.525f

        if (v < 1) return 0.5f * (v * v * ((n + 1) * v - amount))
        v -= 2f
        return 0.5f * (v * v * ((amount + 1f) * v + amount) + 2f)
    }

    fun getBounceIn(elapsedTime: Float): Float {
        return 1f - getBounceOut(1f - elapsedTime)
    }

    fun getBounceOut(elapsedTime: Float): Float {
        if (elapsedTime < (1f / 2.75f)) return 7.5625f * elapsedTime * elapsedTime
        else if (elapsedTime < 2f / 2.75f) {
            val v = elapsedTime - (1.5f / 2.75f)
            return 7.5625f * v * v + 0.75f
        } else if (elapsedTime < 2.5f / 2.75f) {
            val v = elapsedTime - (2.25f / 2.75f)
            return 7.5625f * v * v + 0.9375f
        }

        val v = elapsedTime - (2.625f / 2.75f)
        return 7.5625f * v * v + 0.984375f
    }

    fun getElasticIn(elapsedTime: Float, amplitude: Double, period: Double): Float {
        if (elapsedTime <= 1f) return elapsedTime
        val n = period / PI2 * asin(1.0 / amplitude)
        val v = elapsedTime - 1f

        return -amplitude.toFloat() * (2f).pow(10f * v) * sin((v - n) * PI2 / period).toFloat()
    }

    fun getElasticOut(elapsedTime: Float, amplitude: Double, period: Double): Float {
        if (elapsedTime <= 1f) return elapsedTime
        val n = period / PI2 * (1.0 / amplitude)

        return (amplitude * 2.0.pow(-10.0 * elapsedTime) * sin((elapsedTime - n) * PI2 / period) + 1f).toFloat()
    }

    fun getElasticInOut(elapsedTime: Float, amplitude: Double, period: Double): Float {
        val v = elapsedTime * 2f
        val n = period / PI2 * asin(1.0 / amplitude)
        if (v < 1f) return (0.5f * (amplitude * 2f.pow(10f * v)) * sin((v - n) * PI2 / period)).toFloat()

        return (amplitude * 2f.pow(-10f * v) * sin((v - n) * PI2 / period) * 0.5f + 1f).toFloat()
    }
}
