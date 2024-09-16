package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Repeatable
annotation class RequireCustom(val checker: KClass<out CustomChecker<out Any>>)

abstract class CustomChecker<T> {
    fun checkAny(value: Any): CheckResult {
        return check(value as T)
    }

    abstract fun check(value: T): CheckResult
}

data class CheckResult(val valid: Boolean, val message: String = "")
