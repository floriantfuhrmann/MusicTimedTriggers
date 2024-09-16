package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

@Target(AnnotationTarget.FIELD)
annotation class RequireDoubleRange(val min: Double = -Double.MAX_VALUE, val max: Double = Double.MAX_VALUE)