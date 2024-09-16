package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

@Target(AnnotationTarget.FIELD)
annotation class PlusMinusButtons(val step: Double = 1.0)