package com.won983212.mcssync.mutex

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI

class NetworkMutex(mutexUrl: String) {

    private val checkApi = URI(mutexUrl)
    private val lockApi = checkApi.resolve("/lock")
    private val unlockApi = checkApi.resolve("/unlock")

    fun isLocked(): Boolean {
        return get(checkApi) == "locked"
    }

    fun lock() {
        get(lockApi)
    }

    fun unlock() {
        get(unlockApi)
    }

    private fun get(uri: URI): String {
        val connection = uri.toURL().openConnection()
        BufferedReader(InputStreamReader(connection.getInputStream())).use {
            return it.readText()
        }
    }
}