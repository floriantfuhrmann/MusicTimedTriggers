package eu.florian_fuhrmann.musictimedtriggers.triggers.templates

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import java.util.*

/**
 * The Base for all Triggers which can be placed in a Song.
 * @param configuration All Triggers can have a configuration
 */
abstract class AbstractTriggerTemplate(
    val uuid: UUID, // uuid of this template
    val configuration: TriggerTemplateConfiguration,
    var group: TriggerTemplateGroup
) {

    /**
     * Returns the Triggers Name (displayed in the Triggers Browser and on
     * Placed Triggers)
     */
    abstract fun name(): String

    abstract fun getType(): TriggerType

    /** Creates a copy of this Trigger Template for another group */
    abstract fun copy(): AbstractTriggerTemplate

    /**
     * Creates a JsonObject containing all info of this template. Json has to
     * contain type and uuid. Custom data is added by #toJson(JsonObject).
     */
    fun toJson(): JsonObject {
        //create json with type and uuid property
        val json = JsonObject()
        json.add("type", JsonPrimitive(getType().name))
        json.add("uuid", JsonPrimitive(uuid.toString()))
        //add rest of information
        toJson(json)
        //return
        return json
    }

    /**
     * Adds Template specific data to json object
     */
    abstract fun toJson(json: JsonObject)

    abstract fun createPlaced(startTime: Double, duration: Double): AbstractPlacedTrigger

    abstract fun getPlacedFromJson(startTime: Double, duration: Double, json: JsonObject): AbstractPlacedTrigger

    companion object {
        fun fromJson(group: TriggerTemplateGroup, json: JsonObject): AbstractTriggerTemplate {
            val triggerType = TriggerType.valueOf(json.get("type").asString)
            val uuid = UUID.fromString(json.get("uuid").asString)
            return when(triggerType) {
                TriggerType.TEST_ON_OFF -> {
                    TestOnOffTriggerTemplate(
                        uuid,
                        TestOnOffTriggerTemplateConfiguration.fromJson(json.get("configuration").asJsonObject),
                        group
                    )
                }
                TriggerType.TEST_GREETING -> {
                    TestGreetingTriggerTemplate(
                        uuid,
                        TestGreetingTriggerTemplateConfiguration.fromJson(json.get("configuration").asJsonObject),
                        group
                    )
                }
                TriggerType.PRINT_INTENSITY -> {
                    PrintIntensityTriggerTemplate(
                        uuid,
                        PrintIntensityTriggerTemplate.TemplateConfiguration.fromJson(json.get("configuration").asJsonObject),
                        group
                    )
                }
            }
        }

        fun create(triggerType: TriggerType, group: TriggerTemplateGroup): AbstractTriggerTemplate {
            val uuid = UUID.randomUUID()
            return when(triggerType) {
                TriggerType.TEST_ON_OFF -> {
                    TestOnOffTriggerTemplate(
                        uuid,
                        TestOnOffTriggerTemplateConfiguration.create(),
                        group
                    )
                }
                TriggerType.TEST_GREETING -> {
                    TestGreetingTriggerTemplate(
                        uuid,
                        TestGreetingTriggerTemplateConfiguration(),
                        group
                    )
                }
                TriggerType.PRINT_INTENSITY -> {
                    PrintIntensityTriggerTemplate(
                        uuid,
                        PrintIntensityTriggerTemplate.TemplateConfiguration(),
                        group
                    )
                }
            }
        }
    }

}

open class TriggerTemplateConfiguration(
    @Configurable("Color", "Color for this Trigger")
    var color: GenericColor = GenericColor(0, 0, 0, 255)
) : Configuration() {
    /**
     * Can be used to copy this TriggerTemplateConfiguration by setting vars to values from [source]
     * @return itself
     */
    fun copiedFrom(source: TriggerTemplateConfiguration) {
        color = source.color
    }
}
