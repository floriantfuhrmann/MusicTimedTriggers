package eu.florian_fuhrmann.musictimedtriggers.utils

import org.jetbrains.jewel.ui.icon.PathIconKey

object IconsDummy {
    fun getPathIconKeyFor(path: String) = PathIconKey(path, IconsDummy::class.java)
}
