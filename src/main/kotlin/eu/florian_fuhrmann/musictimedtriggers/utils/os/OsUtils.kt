package eu.florian_fuhrmann.musictimedtriggers.utils.os

object OsUtils {

    val osName = System.getProperty("os.name")
    val isWindows: Boolean
        get() = osName.startsWith("Windows")
    val isMacOs: Boolean
        get() = osName.startsWith("Mac OS")

}
