package com.kitsuneo.bquick.ui.util

fun Int.asClock(): String {
    val totalSeconds = coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
