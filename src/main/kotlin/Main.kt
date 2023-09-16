import screepsai.gameLoop

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
@Suppress("unused")
fun loop() {
    gameLoop()
}
