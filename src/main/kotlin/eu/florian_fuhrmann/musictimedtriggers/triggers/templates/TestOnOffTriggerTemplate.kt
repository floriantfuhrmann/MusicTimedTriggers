package eu.florian_fuhrmann.musictimedtriggers.triggers.templates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.PlacedTestOnOffTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.RequireIntRange
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON
import java.util.*

/**
 * The Test On Off Trigger is meant for testing and just prints a message to console when turned on / off
 */
class TestOnOffTriggerTemplate(uuid: UUID, configuration: TestOnOffTriggerTemplateConfiguration, group: TriggerTemplateGroup) :
    AbstractTriggerTemplate(uuid, configuration, group) {

    private fun config() = configuration as TestOnOffTriggerTemplateConfiguration

    override fun name(): String {
        return config().name
    }

    override fun getType(): TriggerType = TriggerType.TEST_ON_OFF

    override fun copy() = TestOnOffTriggerTemplate(UUID.randomUUID(), config().deepCopy(), group)

    override fun toJson(json: JsonObject) {
        //add configuration to json
        json.add("configuration", config().toJson())
    }

    override fun createPlaced(startTime: Double, duration: Double): AbstractPlacedTrigger {
        return PlacedTestOnOffTrigger.create(this, startTime, duration)
    }

    override fun getPlacedFromJson(startTime: Double, duration: Double, json: JsonObject): AbstractPlacedTrigger {
        return PlacedTestOnOffTrigger(this, startTime, duration)
    }
}

data class TestOnOffTriggerTemplateConfiguration(
    @Configurable("Name", "Name for this Debug On/Off Trigger")
    @RequireIntRange(1, 100)
    val name: String
) : TriggerTemplateConfiguration() {
    fun toJson(): JsonObject {
        return JsonParser.parseString(GSON.toJson(this)).asJsonObject
    }

    fun deepCopy(): TestOnOffTriggerTemplateConfiguration {
        val copy = copy()
        copy.copiedFrom(this)
        return copy
    }

    companion object {
        fun fromJson(json: JsonElement): TestOnOffTriggerTemplateConfiguration {
            return GSON.fromJson(json, TestOnOffTriggerTemplateConfiguration::class.java)
        }

        fun create(): TestOnOffTriggerTemplateConfiguration {
            return TestOnOffTriggerTemplateConfiguration("Example Name")
        }
    }
}
