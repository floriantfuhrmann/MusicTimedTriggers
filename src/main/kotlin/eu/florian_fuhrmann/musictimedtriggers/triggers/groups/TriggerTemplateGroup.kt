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
        //save trigger template group to file
        saveToFile(ProjectManager.currentProject!!.projectDirectory)
    }

    /**
     * Save trigger template group to json file inside the trigger templates
     * directory inside the project directory.
     */
    fun saveToFile(projectDirectory: File) {
        //get file
        val file = getTemplateGroupFileInProjectDirectory(projectDirectory, uuid)
        //save json to file
        file.writeText(GSON_PRETTY.toJson(toJson()))
    }

    /**
     * Delete the json file for this trigger template group.
     */
    fun deleteFile(projectDirectory: File) {
        //get file
        val file = getTemplateGroupFileInProjectDirectory(projectDirectory, uuid)
        //delete file
        file.delete()
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
         * Get the file for the trigger template group with the given uuid in the project directory.
         */
        fun getTemplateGroupFileInProjectDirectory(projectDirectory: File, uuid: UUID) =
            File(projectDirectory, TEMPLATE_GROUPS_DIRECTORY_NAME + File.separator + "$uuid.json")

        /**
         * Reads json from file and deserializes trigger template group.
         */
        fun loadFromFile(templateGroupJsonFile: File, uuid: UUID): TriggerTemplateGroup {
            //load json from file
            val json = JsonParser.parseString(templateGroupJsonFile.readText()).asJsonObject
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