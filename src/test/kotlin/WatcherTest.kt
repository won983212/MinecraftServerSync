import com.won983212.mcssync.syncer.watcher.FileWatcher
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class WatcherTest {
    @Test
    fun test() {
        /*val watcher = FileWatcher(Paths.get("C:\\Users\\psvm\\Desktop\\새 폴더"))
        println("Go watching")
        watcher.poll()
        watcher.setOnFileChanges { path, event ->
            println("${event.kind().name()}: $path")
        }
        while (true) {
            println("Hello")
            Thread.sleep(2000)
        }*/
        val path1 = Paths.get("C:\\Users\\psvm\\Desktop\\새 폴더\\")
        val path2 = Paths.get("C:\\Users\\psvm\\Desktop\\1/2\\3\\backup\\")
        val file = Paths.get("C:\\Users\\psvm\\Desktop\\새 폴더\\aa\\bb\\cc")
        println(path2.resolve(path1.relativize(file)))
    }
}