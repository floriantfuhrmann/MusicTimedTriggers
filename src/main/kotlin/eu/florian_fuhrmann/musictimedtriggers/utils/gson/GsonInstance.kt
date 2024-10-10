package eu.florian_fuhrmann.musictimedtriggers.utils.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val GSON = Gson()
val GSON_PRETTY: Gson = GsonBuilder().setPrettyPrinting().create()
