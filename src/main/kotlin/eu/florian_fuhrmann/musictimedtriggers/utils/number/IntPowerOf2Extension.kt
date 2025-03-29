package eu.florian_fuhrmann.musictimedtriggers.utils.number

fun Int.isPowerOf2() = this and (this - 1) == 0
fun Int.isPowerOf2OrZero() = this == 0 || this.isPowerOf2()
