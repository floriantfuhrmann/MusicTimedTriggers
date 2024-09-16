package eu.florian_fuhrmann.musictimedtriggers.triggers.templates

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration.ConfigurationDialog
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.utils.ConfigurationColor
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
     * Returns the Triggers Name (displayed in the Triggers Browser and on Placed Triggers)
     */
    abstract fun name(): String

    abstract fun getType(): TriggerType

    /**
     * Called when the User wants to edit this Trigger (mostly through the Triggers Browser)
     * By Default a ConfigurationDialog will be opened with the configuration.
     *
     * When Implementing your own Edit Dialog make sure, that if [creating] is true the trigger is added to the triggers
     * group with [TriggerTemplateGroup.addTemplate] when the user confirms creation. This will also take care of saving
     * the project.
     * But if [creating] is false then the trigger needs to be updated in the ui using
     * [BrowserState.updateTriggerTemplate] after editing is complete and the project needs to be saved using
     * [Project.save]
     *
     * @param creating Specifies weither this template is currently being created. If so the template is not yet added
     * to the groups templates list and should be done so once the user is finished editing.
     */
    fun openEditDialog(creating: Boolean) {
        DialogManager.openDialog(ConfigurationDialog(
            configuration = configuration,
            heading = if (creating) {
                "Create ${getType().displayName} Template"
            } else {
                "Configuring ${name()}"
            },
            showCancelButton = creating, //cancel is only possible when the trigger template is currently being created
            onDone = {
                if(creating) {
                    //add trigger to group, that also should update the ui
                    ProjectManager.currentProject!!.triggersManager.addTriggerTemplate(this)
                }
            },
            onClose = {
                //if the template is not being created (so already exists in the group) then upon closing it also needs
                // to be updated because changes are applied anyway
                if(!creating) {
                    ProjectManager.currentProject?.browserState?.updateTriggerTemplate(this)
                    //save project
                    ProjectManager.currentProject?.save()
                }
            }
        ))
    }

    /**
     * Creates a copy of this Trigger Template for another group
     */
    abstract fun copy(): AbstractTriggerTemplate

    /**
     * Creates a JsonObject containing all info of this template. Json has to contain type and uuid. Custom data is
     * added by #toJson(JsonObject).
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
            }
        }
    }

}

open class TriggerTemplateConfiguration(
    @Configurable("Color", "Color for this Trigger")
    var color: ConfigurationColor = ConfigurationColor(0, 0, 0, 255)
) : Configuration() {
    /**
     * Can be used to copy this TriggerTemplateConfiguration by setting vars to values from [source]
     * @return itself
     */
    fun copiedFrom(source: TriggerTemplateConfiguration) {
        color = source.color
    }
}
