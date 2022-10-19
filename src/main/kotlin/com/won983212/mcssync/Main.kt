package com.won983212.mcssync

import com.won983212.mcssync.config.ProgramConfig
import com.won983212.mcssync.git.GitExec
import com.won983212.mcssync.git.GitExecException
import com.won983212.mcssync.git.UserAuth
import com.won983212.mcssync.mutex.NetworkMutex
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
    private val git: GitExec
    private val config: ProgramConfig
    private val userAuth: UserAuth
    private val mutex: NetworkMutex
    private var useMutex = true

    init {
        config = ProgramConfig(File(dir, "sync-config.cfg"))
        userAuth = UserAuth(config.userId, config.userPass, config.userName, config.userEmail)
        mutex = NetworkMutex(config.serverHost)

        git = GitExec(userAuth)
        git.setProgressMonitor(TextProgressMonitor(PrintWriter(System.out)))
        git.open(dir)
    }

    private fun checkLock(): Boolean {
        try {
            if (mutex.isLocked()) {
                println("이미 다른 서버가 열려있습니다.")
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println()
            print("다른 서버가 열려있는지 알 수 없습니다. 그래도 강제로 여시겠습니까? (y/n): ")
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
        println("다른 서버가 열려있는지 확인중...")
        if (!checkLock()) {
            return
        }

        try {
            val startTime = LocalDateTime.now()
            if (useMutex) {
                mutex.lock()
            }

            println("Git 초기화중...")
            if (!git.findRemote("origin")) {
                throw GitExecException("Can't find origin remote")
            }
            git.checkoutBranch(config.branch)

            println("GIT 서버와 파일 동기화 중...")
            git.pull()
            println("동기화가 완료되었습니다. 이제 서버를 시작합니다!!")

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
                    println("에러가 발생해서 서버를 중단합니다.")
                }
            } finally {
                val endTime = LocalDateTime.now()
                val endTimeString = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val durationString = formatDuration(Duration.between(startTime, endTime))

                println("변경사항들을 저장합니다... 저장중에는 절대 종료하지마세요!!")

                git.add(".")
                val changes = git.countChanges()
                if (changes > 0) {
                    println("변경된 파일: ${changes}개")
                    git.commit("$endTimeString (Playing time: $durationString)")
                    git.push()
                    println("저장완료!")
                } else {
                    println("변경된 파일이 없으므로 저장하지않습니다.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("에러가 발생해서 서버를 시작할 수 없습니다.")
        } finally {
            if (useMutex) {
                mutex.unlock()
            }
            println("종료작업 완료!")
        }
    }
}

fun main() {
    Main().run()
}