import screepsai.gameLoop

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@JsExport
@Suppress("unused")
fun loop() {
    gameLoop()
}
