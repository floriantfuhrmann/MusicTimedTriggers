package eu.florian_fuhrmann.musictimedtriggers.triggers.groups

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
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
        ProjectManager.currentProject?.save()
    }

    fun toJson(): JsonObject {
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
        fun createDefaultGroup(): TriggerTemplateGroup {
            return TriggerTemplateGroup(UUID(0L, 0L), "Default", mutableListOf())
        }
        fun createNewGroup(name: String): TriggerTemplateGroup {
            return TriggerTemplateGroup(UUID.randomUUID(), name, mutableListOf())
        }

        fun fromJson(uuid: UUID, json: JsonObject): TriggerTemplateGroup {
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