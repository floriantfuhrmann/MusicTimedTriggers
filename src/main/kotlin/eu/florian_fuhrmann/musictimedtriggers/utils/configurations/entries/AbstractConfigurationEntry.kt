package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.CheckResult
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.RequireCustom
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.VisibleWhen
import java.lang.reflect.Field

abstract class AbstractConfigurationEntry<T : Any>(
    val configuration: Configuration,
    val field: Field,
    val configurable: Configurable,
    private val customCheckers: List<RequireCustom> = emptyList(),
    private val visibleWhen: VisibleWhen? = null,
    val visible: MutableState<Boolean> = mutableStateOf( true )
) {
    init {
        //update visible in init to get initial visible value
        updateVisible()
    }

    @Composable
    abstract fun Content()

    fun checkCustom(value: T): CheckResult {
        return customCheckers.map {
            it.checker.objectInstance!!.checkAny(value)
        }.find {
            !it.valid
        } ?: CheckResult(true, "")
    }

    /**
     * Called when the value for this entry is changed
     */
    fun handleValueChanged() {
        //recheck visible for alle entries
        configuration.entries?.forEach {
            it.updateVisible()
        }
    }

    private fun updateVisible() {
        visible.value = isVisible()
    }
    private fun isVisible(): Boolean {
        if(visibleWhen == null) return true
        val result = visibleWhen.checker.objectInstance!!.checkAny(configuration)
        return (result && !visibleWhen.inverted) || (!result && visibleWhen.inverted)
    }
}
