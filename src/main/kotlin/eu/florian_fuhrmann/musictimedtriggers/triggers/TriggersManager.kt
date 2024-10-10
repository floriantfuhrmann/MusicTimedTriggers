package eu.florian_fuhrmann.musictimedtriggers.triggers

import com.google.gson.JsonObject
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import java.io.File
import java.util.*

class TriggersManager(
    private val triggerTemplates: MutableMap<UUID, AbstractTriggerTemplate>, //mapping trigger template uuid to trigger
    private val triggerTemplateGroups: MutableMap<UUID, TriggerTemplateGroup> //mapping group uuid to group (group contains ordered list of trigger templates)
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
        // TODO
    }
    fun updateTriggerTemplateGroup(triggerTemplateGroup: TriggerTemplateGroup, newName: String) {
        //set new name
        triggerTemplateGroup.name = newName
        //update ui
        ProjectManager.currentProject?.browserState?.updateGroup(triggerTemplateGroup)
        //save project
        // TODO
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
        // TODO
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
        // TODO
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
        // TODO
    }

    // Deserializing Placed Triggers

    /**
     * Deserialize a placed trigger from a json
     */
    fun getPlacedTriggerFromJson(json: JsonObject): AbstractPlacedTrigger {
        // get trigger template
        val triggerTemplateUuid = UUID.fromString(json.get("template").asString)
        val triggerTemplate = getTriggerTemplate(triggerTemplateUuid)
        require(triggerTemplate != null) { "No Trigger Template $triggerTemplateUuid exists" }
        // get start time and duration
        val startTime: Double = json.get("startTime").asDouble
        val duration: Double = json.get("duration").asDouble
        // get placed trigger instance from trigger template
        return triggerTemplate.getPlacedFromJson(startTime, duration, json)
    }

    // Saving and Loading

    /**
     * Create the template groups directory inside the project directory.
     * Function intended to be used during project creation.
     */
    fun createTemplateGroupsDirectory(projectDirectory: File) {
        // create template groups directory
        val templateGroupsDirectory = File(projectDirectory, TriggerTemplateGroup.TEMPLATE_GROUPS_DIRECTORY_NAME)
        templateGroupsDirectory.mkdir()
    }

    /**
     * Save all trigger template groups to json files inside the template groups directory
     */
    fun saveAllTemplateGroupsToFile(projectDirectory: File) {
        //save all trigger template groups
        triggerTemplateGroups.values.forEach {
            it.saveToFile(projectDirectory)
        }
    }

    companion object {
        fun create(): TriggersManager {
            val defaultGroup = TriggerTemplateGroup.createDefaultGroup()
            return TriggersManager(mutableMapOf(), mutableMapOf(defaultGroup.uuid to defaultGroup))
        }

        /**
         * Load all trigger template groups from json files inside the template groups directory
         */
        fun loadFromFiles(projectDirectory: File): TriggersManager {
            // get template groups directory
            val templateGroupsDirectory = File(projectDirectory, TriggerTemplateGroup.TEMPLATE_GROUPS_DIRECTORY_NAME)
            // make sure the directory exists
            require(templateGroupsDirectory.exists()) { "Template Groups Directory does not exist" }
            // load all trigger template groups
            val triggerTemplateGroups = templateGroupsDirectory.listFiles()!!.map {
                // skip non json files
                if (it.extension != "json") return@map null
                // get uuid from file name
                val uuid = UUID.fromString(it.nameWithoutExtension)
                // load group from file
                TriggerTemplateGroup.loadFromFile(it, uuid)
            }.filterNotNull().associateBy(keySelector = { it.uuid }, valueTransform = { it }).toMutableMap()
            // create templates map from group
            val triggerTemplates = triggerTemplateGroups.values.flatMap {
                it.templates
            }.associateBy(keySelector = { it.uuid }, valueTransform = { it }).toMutableMap()
            // return
            return TriggersManager(triggerTemplates, triggerTemplateGroups)
        }
    }

}