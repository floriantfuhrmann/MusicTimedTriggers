package eu.florian_fuhrmann.musictimedtriggers.utils.configurations

import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import java.lang.reflect.Field

/**
 * Transports some context information to the configuration entries.
 */
abstract class ConfigurationContext

/**
 * An empty configuration context. This is used when no context is needed.
 */
class EmptyConfigurationContext : ConfigurationContext()

/**
 * A context for change listeners. This is used when a configuration entry needs to notify the context about changes.
 */
interface ChangeListenerContext {
    fun onChange(field: Field, configurable: Configurable)
}