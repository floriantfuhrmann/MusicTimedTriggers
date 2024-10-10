package eu.florian_fuhrmann.musictimedtriggers.triggers.groups

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.util.UUID

class TriggerTemplateGroup(
    val uuid: UUID, // uuid for this group
    var name: String,
    val templates: MutableList<AbstractTriggerTemplate>
) {

    fun moveTemplate(fromIndex: Int, toIndex: Int) {
        //update list
        templates.add(toIndex, templates.removeAt(fromIndex))
        //update ui
        ProjectManager.currentProject?.browserState?.moveTemplate(fromIndex, toIndex)
        //save project
        // TODO
    }

    /**
     * Save trigger template group to json file inside the trigger templates
     * directory inside the project directory.
     */
    fun saveToFile(projectDirectory: File) {
        //get file
        val file = File(projectDirectory, TEMPLATE_GROUPS_DIRECTORY_NAME + File.separator + "$uuid.json")
        //save json to file
        file.writeText(GSON_PRETTY.toJson(toJson()))
    }

    private fun toJson(): JsonObject {
        val json = JsonObject()
        //add name
        json.add("name", JsonPrimitive(name))
        //add templates
        val templatesJsonArray = JsonArray()
        templates.forEach {
            templatesJsonArray.add(it.toJson())
        }
        json.add("triggerTemplates", templatesJsonArray)
        //return json
        return json
    }

    companion object {
        const val TEMPLATE_GROUPS_DIRECTORY_NAME = "TemplateGroups"

        fun createDefaultGroup(): TriggerTemplateGroup {
            return TriggerTemplateGroup(UUID(0L, 0L), "Default", mutableListOf())
        }

        fun createNewGroup(name: String): TriggerTemplateGroup {
            return TriggerTemplateGroup(UUID.randomUUID(), name, mutableListOf())
        }

        /**
         * Load trigger template group from json file inside the trigger templates
         * directory inside the project directory.
         */
        fun loadFromFile(projectDirectory: File, uuid: UUID): TriggerTemplateGroup {
            //get file
            val file = File(projectDirectory, TEMPLATE_GROUPS_DIRECTORY_NAME + File.separator + "$uuid.json")
            //load json from file
            val json = JsonParser.parseString(file.readText()).asJsonObject
            //deserialize template group from json
            return fromJson(uuid, json)
        }

        private fun fromJson(uuid: UUID, json: JsonObject): TriggerTemplateGroup {
            //get name
            val name = json.get("name").asString
            //init trigger templates list
            val templates: MutableList<AbstractTriggerTemplate> = mutableListOf()
            //create group instance
            val group = TriggerTemplateGroup(uuid, name, templates)
            //add trigger templates
            json.get("triggerTemplates").asJsonArray.forEach {
                templates.add(AbstractTriggerTemplate.fromJson(group, it.asJsonObject))
            }
            //return group
            return group
        }
    }

}