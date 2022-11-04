package com.won983212.mcssync.syncer.watcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.*

class FileWatcher(watchPath: Path) {

    private val service: WatchService = FileSystems.getDefault().newWatchService()
    private var fileChangeListener: FileChangeListener? = null

    init {
        watchPath.register(
            service,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
    }

    fun setOnFileChanges(listener: FileChangeListener) {
        fileChangeListener = listener
    }

    fun poll() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val key = withContext(Dispatchers.IO) {
                    service.take()
                }
                val events = key.pollEvents()
                for (event in events) {
                    val path = event.context() as Path
                    fileChangeListener?.onFileChange(path, event)
                }
                if (!key.reset())
                    break;
            }
            withContext(Dispatchers.IO) {
                service.close()
            }
        }
    }

    fun interface FileChangeListener {
        fun onFileChange(path: Path, event: WatchEvent<*>)
    }
}