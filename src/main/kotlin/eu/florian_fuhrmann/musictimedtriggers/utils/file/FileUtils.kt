package eu.florian_fuhrmann.musictimedtriggers.utils.file

import java.io.File

fun findAvailableTargetFile(directory: File, fileName: String): File {
    //init counter
    var counter = 0
    //init f with initial file name
    var f = File(directory, fileName)
    //find name and extensions part
    val fileNameWithoutExtensions = getFileNameWithoutExtensions(fileName)
    val fileExtensions = getFileNameExtensions(fileName)
    //count up until a free name is found
    while (f.exists()) {
        counter++
        f = File(directory, "${fileNameWithoutExtensions}_${counter}$fileExtensions")
        //prevent infinite loop
        if(counter == Int.MAX_VALUE) {
            throw IllegalStateException("counter for finding available file name reached invalid range")
        }
    }
    return f
}

fun getStringWithoutSpecialChars(string: String) = string
    .replace(' ', '_') // turn spaces into underscores
    .replace(Regex("[^a-zA-Z0-9_-]"), "") //remove any special chars
    .replace("_-", "_").replace("-_", "_") //avoid stuff like 'abc-_abc', 'abc_-abc', 'abc_-_-_abc'
    .replace(Regex("_+"), "_") //replace multiple underscores with just one
fun getFileNameWithoutExtensions(fileName: String) = fileName.substring(0, fileName.indexOf('.'))
fun getFileNameExtensions(fileName: String) = fileName.substring(fileName.indexOf('.'), fileName.length)
