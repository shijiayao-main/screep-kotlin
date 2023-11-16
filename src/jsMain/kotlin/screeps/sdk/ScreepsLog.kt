package screeps.sdk

object ScreepsLog {
    fun d(tag: String, message: String) {
        console.log("$tag, $message")
    }
}