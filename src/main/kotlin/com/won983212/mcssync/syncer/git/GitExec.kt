package com.won983212.mcssync.syncer.watcher.git

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File


class GitExec(private val userAuth: UserAuth) {
    private val cp: CredentialsProvider = UsernamePasswordCredentialsProvider(userAuth.userId, userAuth.userPass)
    private var gitContext: Git? = null
    private var progressMonitor: ProgressMonitor? = null

    fun open(dir: File) {
        gitContext = try {
            Git.open(dir)
        } catch (e: RepositoryNotFoundException) {
            throw GitExecException("Can't find com.won983212.mcssync.git repository.")
        }
    }

    fun setProgressMonitor(progressMonitor: ProgressMonitor) {
        this.progressMonitor = progressMonitor
    }

    fun checkoutBranch(branch: String) {
        requireGit().checkout()
            .setName(branch)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .setStartPoint(branch)
            .call()
    }

    private fun requireGit(): Git {
        return gitContext ?: throw GitExecException("You must open com.won983212.mcssync.git directory before use it.")
    }

    fun findRemote(url: String): Boolean {
        val command = requireGit().remoteList()
        for (ent in command.call()) {
            if (ent.name == url) {
                return true
            }
        }
        return false
    }

    fun push() {
        requireGit().push()
            .setRemote("origin")
            .setCredentialsProvider(cp)
            .setProgressMonitor(progressMonitor)
            .call()
    }

    fun add(filePattern: String) {
        requireGit().add()
            .addFilepattern(filePattern)
            .call()
    }

    fun countChanges(): Int {
        val result = requireGit().status().call()
        return result.added.size + result.changed.size + result.modified.size + result.removed.size
    }

    fun commit(msg: String) {
        requireGit().commit()
            .setAuthor(userAuth.userName, userAuth.userEmail)
            .setMessage(msg)
            .call()
    }

    fun pull() {
        requireGit().pull()
            .setRemote("origin")
            .setProgressMonitor(progressMonitor)
            .setCredentialsProvider(cp)
            .call()
    }
}