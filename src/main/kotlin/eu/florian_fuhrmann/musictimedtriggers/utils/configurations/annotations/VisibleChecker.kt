package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Repeatable
annotation class VisibleWhen(val checker: KClass<out VisibleChecker<out Configuration>>, val inverted: Boolean = false)

abstract class VisibleChecker<T : Configuration> {
    fun checkAny(value: Any): Boolean {
        return check(value as T)
    }

    abstract fun check(value: T): Boolean
}
