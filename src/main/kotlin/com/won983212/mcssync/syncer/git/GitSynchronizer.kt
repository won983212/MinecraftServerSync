package com.won983212.mcssync.syncer.git

import com.won983212.mcssync.Logger
import com.won983212.mcssync.syncer.watcher.git.GitExec
import com.won983212.mcssync.syncer.watcher.git.GitExecException

class GitSynchronizer(private val git: GitExec) {

    fun pull(branch: String) {
        Logger.info { "Git 초기화중..." }
        if (!git.findRemote("origin")) {
            throw GitExecException("Can't find origin remote")
        }
        git.checkoutBranch(branch)

        Logger.info { "GIT 서버와 파일 동기화 중..." }
        git.pull()
        Logger.info { "동기화가 완료되었습니다. 이제 서버를 시작합니다!!" }
    }

    fun push(message: String) {
        Logger.info { "변경사항들을 저장합니다... 저장중에는 절대 종료하지마세요!!" }

        git.add(".")
        val changes = git.countChanges()
        if (changes > 0) {
            Logger.info { "변경된 파일: ${changes}개" }
            git.commit(message)
            git.push()
            Logger.info { "저장완료!" }
        } else {
            Logger.info { "변경된 파일이 없으므로 저장하지않습니다." }
        }
    }
}