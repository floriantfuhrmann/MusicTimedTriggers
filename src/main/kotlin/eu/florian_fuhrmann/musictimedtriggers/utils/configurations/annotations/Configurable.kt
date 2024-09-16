package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

@Target(AnnotationTarget.FIELD)
annotation class Configurable(val displayName: String, val description: String = "")