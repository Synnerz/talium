package com.github.synnerz.talium.constraints

import com.github.synnerz.talium.components.UIElement

interface UIBaseConstraint<T> {
    // TODO: built in caching
    var value: T
    var parent: UIElement

    fun onUpdate()
}

interface UIXConstraint : UIBaseConstraint<Double> {
    fun x(): Double

    fun x(parent: UIElement): Double
}

interface UIYConstraint : UIBaseConstraint<Double> {
    fun y(): Double

    fun y(parent: UIElement): Double
}

interface UIWidthConstraint : UIBaseConstraint<Double> {
    fun width(): Double

    fun width(parent: UIElement): Double
}

interface UIHeightConstraint : UIBaseConstraint<Double> {
    fun height(): Double

    fun height(parent: UIElement): Double
}

/** * Constraint with x and y */
interface UIPositionConstraint : UIXConstraint, UIYConstraint
/** * Constraint with width and height */
interface UISizeConstraint : UIWidthConstraint, UIHeightConstraint
/** * Constraint with x, y, width and height */
interface UIConstraint : UIPositionConstraint, UISizeConstraint
