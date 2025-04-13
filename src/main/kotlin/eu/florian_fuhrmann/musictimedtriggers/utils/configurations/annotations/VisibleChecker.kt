package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import kotlin.reflect.KClass

/**
 * Annotation to pass the class of a [VisibleChecker] to a configurable.
 * This checker is used to determine whether the configurable should be
 * visible or not in the configuration box. Can for example be used to hide
 * configurables when they are not relevant due to the value of another
 * configurable.
 */
@Target(AnnotationTarget.FIELD)
annotation class VisibleWhen(val checker: KClass<out VisibleChecker<out Configuration>>, val inverted: Boolean = false)

abstract class VisibleChecker<T : Configuration> {
    fun checkAny(value: Any): Boolean {
        return check(value as T)
    }

    abstract fun check(value: T): Boolean
}
