package eu.florian_fuhrmann.musictimedtriggers.utils.configurations

import com.godaddy.android.colorpicker.HsvColor
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries.*
import java.lang.reflect.Field

abstract class Configuration {

    @Transient
    var entries: List<AbstractConfigurationEntry<out Any>>? = null

    fun createOrGetEntries(context: ConfigurationContext): List<AbstractConfigurationEntry<out Any>> {
        if(entries == null) {
            entries = createEntries(context)
        }
        return entries!!
    }

    private fun createEntries(context: ConfigurationContext): List<AbstractConfigurationEntry<out Any>> {
        return getAllFields().mapNotNull { field ->
            val configurable = field.annotations.find { it.annotationClass == Configurable::class } as? Configurable
            if (configurable != null && field.trySetAccessible()) {
                createConfigurationEntry(field, configurable, context)
            } else {
                null
            }
        }
    }

    private fun getAllFields(): List<Field> {
        val result = this.javaClass.declaredFields.toMutableList()
        var superclass: Class<*> = this.javaClass.superclass
        while (superclass != Configuration::class.java && superclass != Object::class.java) {
            result.addAll(superclass.declaredFields)
            superclass = superclass.superclass
        }
        return result
    }

    private fun createConfigurationEntry(field: Field, configurable: Configurable, context: ConfigurationContext): AbstractConfigurationEntry<out Any> {
        //find custom checkers
        val customCheckers = field.annotations.filter {
            it.annotationClass == RequireCustom::class
        }.map {
            it as RequireCustom
        }.toList()
        //get visible when
        val visibleWhen = field.annotations.find { it.annotationClass == VisibleWhen::class } as? VisibleWhen
        //create entry depending on type
        return when (field.type) {
            Int::class.java -> {
                val intRange = field.annotations.find { it.annotationClass == RequireIntRange::class } as? RequireIntRange
                val buttonParams = field.annotations.find { it.annotationClass == PlusMinusButtons::class } as? PlusMinusButtons
                IntegerConfigurationEntry(this, field, configurable, customCheckers, visibleWhen, intRange, buttonParams)
            }
            Double::class.java -> {
                val doubleRange = field.annotations.find { it.annotationClass == RequireDoubleRange::class } as? RequireDoubleRange
                val buttonParams = field.annotations.find { it.annotationClass == PlusMinusButtons::class } as? PlusMinusButtons
                DoubleConfigurationEntry(this, field, configurable, customCheckers, visibleWhen, doubleRange, buttonParams)
            }
            Boolean::class.java -> {
                BooleanConfigurationEntry(this, field, configurable, customCheckers, visibleWhen)
            }
            String::class.java -> {
                val intRange = field.annotations.find { it.annotationClass == RequireIntRange::class } as? RequireIntRange
                StringConfigurationEntry(this, field, configurable, customCheckers, visibleWhen, intRange)
            }
            java.awt.Color::class.java, androidx.compose.ui.graphics.Color::class.java, HsvColor::class.java, GenericColor::class.java -> {
                val showAlphaBar = field.annotations.find { it.annotationClass == ShowAlphaBar::class } as? ShowAlphaBar
                ColorConfigurationEntry(
                    this,
                    field,
                    configurable,
                    customCheckers,
                    visibleWhen,
                    showAlphaBar != null && showAlphaBar.showAlphaBar
                )
            }
            Keyframes::class.java -> {
                KeyframesConfigurationEntry(this, field, configurable, visibleWhen, context)
            }
            else -> {
                ErrorConfigurationEntry(this, field, configurable)
            }
        }
    }
}