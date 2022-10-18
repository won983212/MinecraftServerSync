package config

import java.io.File
import java.io.FileReader
import java.util.Properties

class ProgramConfig(file: File) {
    val userId: String
    val userPass: String
    val userName: String
    val userEmail: String
    val remoteUrl: String
    val branch: String
    val serverHost: String

    init {
        val properties = Properties()
        FileReader(file).use {
            properties.load(it)
        }

        userId = getPropertyOrThrow(properties, "userId")
        userPass = getPropertyOrThrow(properties, "userPass")
        userName = getPropertyOrThrow(properties, "userName")
        userEmail = getPropertyOrThrow(properties, "userEmail")
        remoteUrl = getPropertyOrThrow(properties, "remoteUrl")
        branch = getPropertyOrThrow(properties, "branch")
        serverHost = getPropertyOrThrow(properties, "serverHost")
    }

    private fun getPropertyOrThrow(properties: Properties, key: String): String {
        return properties.getProperty(key) ?: throw ConfigParseException("Can't not found config key: $key")
    }
}