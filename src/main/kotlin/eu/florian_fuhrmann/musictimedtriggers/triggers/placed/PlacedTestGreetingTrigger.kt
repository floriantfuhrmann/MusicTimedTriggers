package eu.florian_fuhrmann.musictimedtriggers.triggers.placed

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.TestGreetingTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON

class PlacedTestGreetingTrigger(
    triggerTemplate: TestGreetingTriggerTemplate,
    startTime: Double,
    duration: Double,
    configuration: TestGreetingPlacedTriggerConfiguration
) : AbstractPlacedTrigger(triggerTemplate, startTime, duration, configuration) {

    private fun template() = triggerTemplate as TestGreetingTriggerTemplate
    private fun config() = configuration as TestGreetingPlacedTriggerConfiguration

    override fun name(): String {
        return "${template().config().greeting} ${config().name}"
    }

    override fun on(timePosition: Double) {
        println("${template().config().greeting} ${config().name} (time: $timePosition)")
    }

    companion object {
        fun create(template: TestGreetingTriggerTemplate, startTime: Double, duration: Double): PlacedTestGreetingTrigger {
            return PlacedTestGreetingTrigger(template, startTime, duration, TestGreetingPlacedTriggerConfiguration())
        }
    }
}

data class TestGreetingPlacedTriggerConfiguration(
    @Configurable("Name", "Which name to greet")
    val name: String = "World"
) : Configuration() {
    fun toJson(): JsonObject {
        return JsonParser.parseString(GSON.toJson(this)).asJsonObject
    }

    companion object {
        fun fromJson(json: JsonElement): TestGreetingPlacedTriggerConfiguration {
            return GSON.fromJson(json, TestGreetingPlacedTriggerConfiguration::class.java)
        }
    }
}
