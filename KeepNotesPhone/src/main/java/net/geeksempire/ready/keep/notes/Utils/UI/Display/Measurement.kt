/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 7/24/20 7:09 PM
 * Last modified 7/24/20 6:39 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.ready.keep.notes.Utils.UI.Display

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

fun columnCount(context: Context, itemWidth: Int): Int {

    var spanCount = (displayX(context) / DpToPixel(context, itemWidth.toFloat())).toInt()

    if (spanCount < 1) {
        spanCount = 1
    }

    return spanCount
}

fun DpToPixel(context: Context, dp: Float): Float {
    val resources: Resources = context.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun PixelToDp(context: Context, px: Float): Float {
    val resources: Resources = context.resources
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.DpToPixel(context: Context): Float {
    val resources: Resources = context.resources
    val metrics = resources.displayMetrics
    return this@DpToPixel * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.PixelToDp(context: Context): Float {
    val resources: Resources = context.resources
    val metrics = resources.displayMetrics
    return this@PixelToDp / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun DpToInteger(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

/**
 * Get Touch Pixel Position
 * @param displayEnd = displayX() Or displayY()
 **/
fun Float.pixelToPercentage(displayEnd: Float) : Float {

    return (this@pixelToPercentage * 100) / displayEnd
}

/**
 * Get Percentage Pixel Position
 * @param displayEnd = displayX() Or displayY()
 **/
fun Float.percentageToPixel(displayEnd: Float) : Float {

    return (this@percentageToPixel * displayEnd) / 100
}

fun displayX(context: Context): Int {

    return context.resources.displayMetrics.widthPixels
}

fun displayY(context: Context): Int {

    return context.resources.displayMetrics.heightPixels
}

fun statusBarHeight(context: Context) : Int {

    var statusBarHeight = 0

    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
    }

    return statusBarHeight
}

fun navigationBarHeight(context: Context) : Int {

    var navigationBarHeight = 0

    val resourceIdNavigationBar: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceIdNavigationBar > 0) {
        navigationBarHeight = context.resources.getDimensionPixelSize(resourceIdNavigationBar)
    }

    return navigationBarHeight
}