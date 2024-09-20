package eu.florian_fuhrmann.musictimedtriggers.triggers.templates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.PlacedPrintIntensityTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.RequireIntRange
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON
import java.util.*

/**
 * Triggers using this template will print their intensity to console when
 * updated. This Template thus has no production use and is meant to be
 * used during development.
 */
class PrintIntensityTriggerTemplate(
    uuid: UUID, configuration: TemplateConfiguration,
    group: TriggerTemplateGroup
) : AbstractTriggerTemplate(uuid, configuration, group) {

    fun config() = configuration as TemplateConfiguration
    override fun name() = "Intensity (${config().name})"
    override fun getType(): TriggerType = TriggerType.PRINT_INTENSITY
    override fun copy(): AbstractTriggerTemplate =
        PrintIntensityTriggerTemplate(UUID.randomUUID(), config().deepCopy(), group)

    override fun toJson(json: JsonObject) {
        //add configuration to json
        json.add("configuration", config().toJson())
    }

    override fun createPlaced(startTime: Double, duration: Double) =
        PlacedPrintIntensityTrigger.create(this, startTime, duration)

    override fun getPlacedFromJson(startTime: Double, duration: Double, json: JsonObject) =
        PlacedPrintIntensityTrigger.fromJson(this, startTime, duration, json)

    data class TemplateConfiguration(
        @Configurable("Name", "Name to identify this print intensity trigger")
        @RequireIntRange(1, 100)
        val name: String = "Example"
    ) : TriggerTemplateConfiguration() {
        fun toJson(): JsonObject = JsonParser.parseString(GSON.toJson(this)).asJsonObject

        fun deepCopy(): TemplateConfiguration {
            val copy = copy() // copies this data classes attributes
            copy.copiedFrom(this) // copies attributes from source to copy of inherited configuration
            return copy
        }

        companion object {
            fun fromJson(json: JsonElement): TemplateConfiguration =
                GSON.fromJson(json, TemplateConfiguration::class.java)

            fun create() = TemplateConfiguration("Example Name")
        }
    }
}