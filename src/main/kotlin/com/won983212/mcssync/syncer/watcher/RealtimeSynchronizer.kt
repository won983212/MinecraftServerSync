package com.won983212.mcssync.syncer.watcher

import java.nio.file.*
import kotlin.io.path.notExists

class RealtimeSynchronizer(
    private val originalDir: Path,
    private val backupDir: Path
) : FileWatcher.FileChangeListener {
    private val fileWatcher: FileWatcher

    init {
        if (originalDir.notExists()) {
            throw WatcherException("Can't find original world folder: $originalDir")
        }

        if (backupDir.notExists()) {
            throw WatcherException("Can't find backup world folder: $originalDir")
        }

        fileWatcher = FileWatcher(originalDir)
        fileWatcher.setOnFileChanges(this)
        fileWatcher.poll()
    }

    override fun onFileChange(path: Path, event: WatchEvent<*>) {
        val target = backupDir.resolve(originalDir.relativize(path))
        when (event.kind()) {
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY -> {
                Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING)
            }

            StandardWatchEventKinds.ENTRY_DELETE -> {
                Files.delete(target)
            }
        }
    }
}