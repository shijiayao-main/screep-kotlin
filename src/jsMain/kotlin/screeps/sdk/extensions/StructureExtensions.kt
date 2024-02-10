package screeps.sdk.extensions

import screeps.api.STRUCTURE_ROAD
import screeps.api.structures.Structure

fun Structure?.isPublicBuild(): Boolean {
    val structure = this ?: return false
    return structure.structureType == STRUCTURE_WALL ||
        structure.structureType == STRUCTURE_RAMPART ||
        structure.structureType == STRUCTURE_ROAD
}
