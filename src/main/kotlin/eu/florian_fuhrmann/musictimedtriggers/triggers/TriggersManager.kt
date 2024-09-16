package eu.florian_fuhrmann.musictimedtriggers.triggers

import com.google.gson.JsonObject
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import java.util.*

class TriggersManager(
    private val triggerTemplates: MutableMap<UUID, AbstractTriggerTemplate>, //mapping trigger uuid to trigger
    private val triggerTemplateGroups: MutableMap<UUID, TriggerTemplateGroup> //mapping group uuid to group (group contains ordered list of triggers)
) {

    // Managing Trigger Templates

    fun getTriggerTemplate(uuid: UUID): AbstractTriggerTemplate? {
        return triggerTemplates[uuid]
    }
    fun getTemplateGroup(uuid: UUID): TriggerTemplateGroup? {
        return triggerTemplateGroups[uuid]
    }
    fun getAllTemplateGroups() = triggerTemplateGroups.values
    fun createNewTriggerTemplateGroup(name: String) {
        //create group
        val newGroup = TriggerTemplateGroup.createNewGroup(name)
        triggerTemplateGroups[newGroup.uuid] = newGroup
        //update ui
        ProjectManager.currentProject?.browserState?.newGroup(newGroup)
        //save project
        ProjectManager.currentProject?.save()
    }
    fun updateTriggerTemplateGroup(triggerTemplateGroup: TriggerTemplateGroup, newName: String) {
        //set new name
        triggerTemplateGroup.name = newName
        //update ui
        ProjectManager.currentProject?.browserState?.updateGroup(triggerTemplateGroup)
        //save project
        ProjectManager.currentProject?.save()
    }
    fun deleteTriggerTemplateGroup(triggerTemplateGroup: TriggerTemplateGroup) {
        //remove the templates triggers from the triggers map
        triggerTemplateGroup.templates.forEach {
            triggerTemplates.remove(it.uuid)
        }
        //remove group from group map
        triggerTemplateGroups.remove(triggerTemplateGroup.uuid)
        //update ui
        ProjectManager.currentProject?.browserState?.removeGroup(triggerTemplateGroup)
        //save project
        ProjectManager.currentProject?.save()
    }
    fun addTriggerTemplate(triggerTemplate: AbstractTriggerTemplate) {
        addTriggerTemplates(listOf(triggerTemplate))
    }
    fun addTriggerTemplates(
        addedTriggerTemplates: List<AbstractTriggerTemplate>
    ) {
        addedTriggerTemplates.forEach { triggerTemplate ->
            //make sure the trigger template does not already exist
            require(!triggerTemplates.containsKey(triggerTemplate.uuid)) {
                "Trigger Template with uuid ${triggerTemplate.uuid} is already known."
            }
            require(!triggerTemplate.group.templates.contains(triggerTemplate)) {
                "The Trigger Templates group also has the Trigger Template in the groups templates list."
            }
            //add new trigger template to map and the group's list
            triggerTemplates[triggerTemplate.uuid] = triggerTemplate
            triggerTemplate.group.templates.add(triggerTemplate)
        }
        //update ui
        ProjectManager.currentProject?.browserState?.newTriggerTemplates(addedTriggerTemplates, true)
        //save project
        ProjectManager.currentProject?.save()
    }
    fun removeTriggerTemplates(removedTriggerTemplates: List<AbstractTriggerTemplate>) {
        removedTriggerTemplates.forEach {
            //remove from the groups templates list
            it.group.templates.remove(it)
            //remove from templates map
            triggerTemplates.remove(it.uuid)
        }
        //update ui
        ProjectManager.currentProject?.browserState?.removeTriggerTemplates(removedTriggerTemplates)
        //save project
        ProjectManager.currentProject?.save()
    }

    // Functions for saving and loading

    fun getPlacedTriggerFromJson(json: JsonObject): AbstractPlacedTrigger {
        val triggerTemplateUuid = UUID.fromString(json.get("template").asString)
        val triggerTemplate = getTriggerTemplate(triggerTemplateUuid)
        require(triggerTemplate != null) { "No Trigger Template $triggerTemplateUuid exists" }
        val startTime: Double = json.get("startTime").asDouble
        val duration: Double = json.get("duration").asDouble
        return triggerTemplate.getPlacedFromJson(startTime, duration, json)
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
        //add groups
        val groupsJson = JsonObject()
        triggerTemplateGroups.forEach {
            groupsJson.add(it.key.toString(), it.value.toJson())
        }
        json.add("triggerTemplateGroups", groupsJson)
        return json
    }

    companion object {
        fun fromJson(json: JsonObject): TriggersManager {
            //read groups
            val triggerTemplateGroups = if (json.has("triggerTemplateGroups")) {
                json.get("triggerTemplateGroups").asJsonObject.entrySet().associateBy(keySelector = {
                    UUID.fromString(it.key)
                }, valueTransform = {
                    TriggerTemplateGroup.fromJson(UUID.fromString(it.key), it.value.asJsonObject)
                }).toMutableMap()
            } else {
                val defaultGroup = TriggerTemplateGroup.createDefaultGroup()
                mutableMapOf(defaultGroup.uuid to defaultGroup)
            }
            //create templates map from group
            val triggerTemplates = triggerTemplateGroups.values.flatMap {
                it.templates
            }.associateBy(
                keySelector = { it.uuid },
                valueTransform = { it }
            ).toMutableMap()
            //return
            return TriggersManager(triggerTemplates, triggerTemplateGroups)
        }

        fun create(): TriggersManager {
            val defaultGroup = TriggerTemplateGroup.createDefaultGroup()
            return TriggersManager(mutableMapOf(), mutableMapOf(defaultGroup.uuid to defaultGroup))
        }
    }

}