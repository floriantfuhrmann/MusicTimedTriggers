package eu.florian_fuhrmann.musictimedtriggers.triggers.placed

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.PrintIntensityTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON

class PlacedPrintIntensityTrigger(
    triggerTemplate: PrintIntensityTriggerTemplate,
    startTime: Double,
    duration: Double,
    configuration: PlacedConfiguration
) : AbstractPlacedIntensityTrigger(triggerTemplate, startTime, duration, configuration) {

    private fun template() = triggerTemplate as PrintIntensityTriggerTemplate
    private fun config() = configuration as PlacedConfiguration
    override fun keyframes() = config().keyframes
    override fun name() = "Placed Intensity (${template().config().name})"

    override fun on(timePosition: Double) {
        println("Intensity ON ($timePosition) => ${intensity(timePosition)}")
    }

    override fun update(timePosition: Double) {
        println("Intensity UPDATE ($timePosition) => ${intensity(timePosition)}")
    }

    override fun off(timePosition: Double) {
        println("Intensity OFF ($timePosition) => ${intensity(timePosition)}")
    }

    companion object {
        fun create(
            template: PrintIntensityTriggerTemplate,
            startTime: Double,
            duration: Double
        ): PlacedPrintIntensityTrigger {
            return PlacedPrintIntensityTrigger(
                template,
                startTime,
                duration,
                PlacedConfiguration.create()
            )
        }

        fun fromJson(
            template: PrintIntensityTriggerTemplate,
            startTime: Double,
            duration: Double,
            json: JsonObject
        ): PlacedPrintIntensityTrigger {
            return PlacedPrintIntensityTrigger(
                template,
                startTime,
                duration,
                PlacedConfiguration.fromJson(json.get("configuration") as JsonObject)
            )
        }
    }

    class PlacedConfiguration(
        @Configurable("Keyframes")
        val keyframes: Keyframes
    ) : Configuration() {
        fun toJson(): JsonObject {
            return JsonParser.parseString(GSON.toJson(this)).asJsonObject
        }

        companion object {
            fun create() = PlacedConfiguration(Keyframes.create())
            fun fromJson(json: JsonElement): PlacedConfiguration = GSON.fromJson(json, PlacedConfiguration::class.java)
        }
    }
}