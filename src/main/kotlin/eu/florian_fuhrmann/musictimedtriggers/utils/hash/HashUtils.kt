package eu.florian_fuhrmann.musictimedtriggers.utils.hash

import java.math.BigInteger
import java.security.MessageDigest

// from https://stackoverflow.com/a/66733767/11278956
fun sha256(input: String): String {
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val messageDigest = md.digest(input.toByteArray())

    // Convert byte array into signum representation
    val no = BigInteger(1, messageDigest)

    // Convert message digest into hex value
    var hashtext: String = no.toString(16)

    // Add preceding 0s to make it 64 chars long
    while (hashtext.length < 64) {
        hashtext = "0$hashtext"
    }

    // return the HashText
    return hashtext
}
