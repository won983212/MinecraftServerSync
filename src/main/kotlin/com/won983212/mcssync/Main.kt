package com.won983212.mcssync

import com.won983212.mcssync.config.ProgramConfig
import com.won983212.mcssync.syncer.watcher.git.GitExec
import com.won983212.mcssync.syncer.watcher.git.GitExecException
import com.won983212.mcssync.syncer.watcher.git.UserAuth
import com.won983212.mcssync.mutex.NetworkMutex
import com.won983212.mcssync.syncer.git.GitSynchronizer
import org.eclipse.jgit.lib.TextProgressMonitor
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Scanner

class Main {
    private val dir = File(".")
    private val gitSync: GitSynchronizer
    private val config: ProgramConfig
    private val userAuth: UserAuth
    private val mutex: NetworkMutex
    private var useMutex = true

    init {
        config = ProgramConfig(File(dir, "sync-config.cfg"))
        userAuth = UserAuth(config.userId, config.userPass, config.userName, config.userEmail)
        mutex = NetworkMutex(config.serverHost)

        val git = GitExec(userAuth)
        git.setProgressMonitor(TextProgressMonitor(PrintWriter(System.out)))
        git.open(dir)

        gitSync = GitSynchronizer(git)
    }

    private fun checkLock(): Boolean {
        try {
            if (mutex.isLocked()) {
                Logger.error("이미 다른 서버가 열려있습니다.")
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Logger.warn("다른 서버가 열려있는지 알 수 없습니다. 그래도 강제로 여시겠습니까? (y/n): ")
            BufferedReader(InputStreamReader(System.`in`)).use {
                val forceStart = it.readLine().trim() == "y"
                if (forceStart) {
                    useMutex = false
                }
                return forceStart
            }
        }
        return true
    }

    private fun toTwoDigit(digit: Long): String {
        return if (digit < 10) {
            "0$digit"
        } else {
            digit.toString()
        }
    }

    private fun formatDuration(duration: Duration): String {
        val hours = toTwoDigit(duration.seconds / 3600)
        val minutes = toTwoDigit((duration.seconds % 3600) / 60)
        val seconds = toTwoDigit(duration.seconds % 60)
        return "$hours:$minutes:$seconds"
    }

    fun run() {
        Logger.info("다른 서버가 열려있는지 확인중...")
        if (!checkLock()) {
            return
        }

        try {
            val startTime = LocalDateTime.now()
            if (useMutex) {
                mutex.lock()
            }

            gitSync.pull(config.branch)

            try {
                val builder = ProcessBuilder(config.runCmd)
                builder.redirectErrorStream(true)
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                builder.redirectInput(ProcessBuilder.Redirect.INHERIT)

                val process = builder.start()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
                if (e !is InterruptedException) {
                    Logger.error("에러가 발생해서 서버를 중단합니다.", e)
                }
            } finally {
                val endTime = LocalDateTime.now()
                val endTimeString = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val durationString = formatDuration(Duration.between(startTime, endTime))
                gitSync.push("$endTimeString (Playing time: $durationString)")
            }
        } catch (e: Exception) {
            Logger.error("에러가 발생해서 서버를 시작할 수 없습니다.", e)
        } finally {
            if (useMutex) {
                mutex.unlock()
            }
            Logger.info("종료작업 완료!")
        }
    }
}

fun main() {
    Main().run()
}