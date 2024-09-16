package eu.florian_fuhrmann.musictimedtriggers.triggers.templates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.PlacedTestGreetingTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.TestGreetingPlacedTriggerConfiguration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.RequireIntRange
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON
import java.util.UUID

/**
 * The Test Greeting Trigger is meant for testing and greets a name when turned on. (The Greeting is part of the
 * Trigger, but the name to greet is attribute of the placed version of this Trigger)
 */
class TestGreetingTriggerTemplate(uuid: UUID, configuration: TestGreetingTriggerTemplateConfiguration, group: TriggerTemplateGroup) :
    AbstractTriggerTemplate(uuid, configuration, group) {

    fun config() = configuration as TestGreetingTriggerTemplateConfiguration

    override fun name(): String {
        return "${config().greeting} (Name)"
    }

    override fun getType(): TriggerType = TriggerType.TEST_GREETING

    override fun copy() = TestGreetingTriggerTemplate(UUID.randomUUID(), config().deepCopy(), group)


    override fun toJson(json: JsonObject) {
        //add configuration to json
        json.add("configuration", config().toJson())
    }

    override fun createPlaced(startTime: Double, duration: Double): AbstractPlacedTrigger {
        return PlacedTestGreetingTrigger.create(this, startTime, duration)
    }

    override fun getPlacedFromJson(startTime: Double, duration: Double, json: JsonObject): AbstractPlacedTrigger {
        return PlacedTestGreetingTrigger(
            this,
            startTime,
            duration,
            TestGreetingPlacedTriggerConfiguration.fromJson(json.get("configuration").asJsonObject)
        )
    }

}

data class TestGreetingTriggerTemplateConfiguration(
    @Configurable("Greeting", "What greeting to use")
    @RequireIntRange(1, 100)
    val greeting: String = "Hello"
) : TriggerTemplateConfiguration() {
    fun toJson(): JsonObject {
        return JsonParser.parseString(GSON.toJson(this)).asJsonObject
    }

    fun deepCopy(): TestGreetingTriggerTemplateConfiguration {
        val copy = copy()
        copy.copiedFrom(this)
        return copy
    }

    companion object {
        fun fromJson(json: JsonElement): TestGreetingTriggerTemplateConfiguration {
            return GSON.fromJson(json, TestGreetingTriggerTemplateConfiguration::class.java)
        }
    }
}
