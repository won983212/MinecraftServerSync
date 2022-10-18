package mutex

class NetworkMutex(private val mutexUrl: String) {
    fun isLocked(): Boolean {
        return false
    }

    fun lock() {

    }
}