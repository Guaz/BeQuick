package com.kitsuneo.bquick.ui.util

fun Int.asClock(): String {
    val totalSeconds = coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun Long.asStopwatch(): String {
    val totalMillis = coerceAtLeast(0L)
    val totalSeconds = totalMillis / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = totalMillis % 1_000
    return "%02d:%02d.%03d".format(minutes, seconds, millis)
}

fun String.toClockSecondsOrNull(): Int? {
    val digits = filter(Char::isDigit).takeLast(4)
    if (digits.isEmpty()) return 0

    val normalized = digits.padStart(2, '0')
    val minutes = normalized.dropLast(2).ifEmpty { "0" }.toIntOrNull() ?: return null
    val seconds = normalized.takeLast(2).toIntOrNull() ?: return null
    if (seconds >= 60) return null

    return (minutes * 60) + seconds
}
