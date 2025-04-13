package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

/**
 * Can be applied to most configurables to provide a placeholder text for
 * the input field.
 */
@Target(AnnotationTarget.FIELD)
annotation class PlaceholderText(val text: String)