package com.github.synnerz.talium.animations

interface IAnimation {
    fun getEase(elapsedTime: Float): Float
}
