import config.ProgramConfig
import git.GitExec
import git.GitExecException
import git.UserAuth
import mutex.NetworkMutex
import org.eclipse.jgit.lib.TextProgressMonitor
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Main {
    private val dir = File(".")
    private val git: GitExec
    private val config: ProgramConfig
    private val userAuth: UserAuth
    private val mutex: NetworkMutex

    init {
        config = ProgramConfig(File(dir, "sync-config.cfg"))
        userAuth = UserAuth(config.userId, config.userPass, config.userName, config.userEmail)
        mutex = NetworkMutex(config.serverHost)

        git = GitExec(userAuth)
        git.setProgressMonitor(TextProgressMonitor(PrintWriter(System.out)))
        git.open(File("C:\\Users\\psvm\\Desktop\\새 폴더"))
    }

    private fun checkLock(): Boolean {
        try {
            if (mutex.isLocked()) {
                println("이미 다른 서버가 열려있습니다.")
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            print("다른 서버가 열려있는지 알 수 없습니다. 그래도 강제로 여시겠습니까? (y/n)")
            BufferedReader(InputStreamReader(System.`in`)).use {
                return it.readLine().trim() == "y"
            }
        }
        return true
    }

    private fun findRemote() {
        val origin: String = git.findRemote(config.remoteUrl)
            ?: throw GitExecException("Remote is not matched in config. (in config: ${config.remoteUrl})")
        git.setRemote(origin)
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
        if (!checkLock()) {
            return
        }

        findRemote()
        git.checkoutBranch(config.branch)

        println("GIT 서버와 파일 동기화 중...")
        git.pull()
        println("동기화가 완료되었습니다. 이제 서버를 시작합니다!!")

        val startTime = LocalDateTime.now()

        // running...

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
}

fun main() {
    Main().run()
}