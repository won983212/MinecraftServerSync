package com.won983212.mcssync.config

import java.io.File
import java.io.FileReader
import java.util.Properties

class ProgramConfig(file: File) {
    val userId: String
    val userPass: String
    val userName: String
    val userEmail: String
    val branch: String
    val serverHost: String
    val runCmd: String

    init {
        val properties = Properties()
        FileReader(file).use {
            properties.load(it)
        }

        userId = getPropertyOrThrow(properties, "userId")
        userPass = getPropertyOrThrow(properties, "userPass")
        userName = getPropertyOrThrow(properties, "userName")
        userEmail = getPropertyOrThrow(properties, "userEmail")
        branch = getPropertyOrThrow(properties, "branch")
        serverHost = getPropertyOrThrow(properties, "serverHost")
        runCmd = getPropertyOrThrow(properties, "runCmd")
    }

    private fun getPropertyOrThrow(properties: Properties, key: String): String {
        return properties.getProperty(key) ?: throw ConfigParseException("Can't not found config key: $key")
    }
}