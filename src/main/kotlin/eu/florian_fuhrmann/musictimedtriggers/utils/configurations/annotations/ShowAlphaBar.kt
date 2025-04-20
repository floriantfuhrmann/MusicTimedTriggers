package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

/**
 * Annotation to show an alpha bar for a color picker.
 *
 * @param showAlphaBar Whether to show the alpha bar. (default: true)
 */
@Target(AnnotationTarget.FIELD)
annotation class ShowAlphaBar(val showAlphaBar: Boolean = true)