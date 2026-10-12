package com.example.platform

import kotlin.math.floor

/** Replacement for String.format(Locale.US, "%.1f", value). */
fun formatOneDecimal(value: Double): String {
    val scaled = floor(value * 10.0 + 0.5).toLong()
    val negative = scaled < 0
    val abs = if (negative) -scaled else scaled
    return (if (negative) "-" else "") + "${abs / 10}.${abs % 10}"
}

/** "HH:mm:00" with zero padding. */
fun formatClockTime(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"

/** yyyy-MM-dd in UTC for a millisecond timestamp. */
fun formatIsoDateUtc(millis: Long): String {
    val days = millis.floorDiv(86_400_000L)
    val z = days + 719_468L
    val era = (if (z >= 0) z else z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1_460L + doe / 36_524L - doe / 146_096L) / 365L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = if (mp < 10L) mp + 3L else mp - 9L
    val y = yoe + era * 400L + (if (m <= 2L) 1L else 0L)
    return "${y.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
}

/** Minimal logger (replacement for android.util.Log). */
object Log {
    fun d(tag: String, msg: String) = println("D/$tag: $msg")
    fun i(tag: String, msg: String) = println("I/$tag: $msg")
    fun w(tag: String, msg: String) = println("W/$tag: $msg")
    fun e(tag: String, msg: String) = println("E/$tag: $msg")
}
