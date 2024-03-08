package screeps.sdk.extensions

import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_ROAD
import screeps.api.STRUCTURE_WALL
import screeps.api.StoreOwner
import screeps.api.structures.Structure

fun Structure?.isPublicBuild(): Boolean {
    val structure = this ?: return false
    return structure.structureType == STRUCTURE_WALL ||
        structure.structureType == STRUCTURE_RAMPART ||
        structure.structureType == STRUCTURE_ROAD
}

fun StoreOwner.energyIsFull(): Boolean {
    return store.getFreeCapacity() == store.getUsedCapacity()
}

fun Structure?.isNeedRepair(): Boolean {
    val structure = this ?: return false
    val hits: Int = structure.hits
    val maxHits: Int = structure.hitsMax
    val hitPercent: Float = hits / maxHits.toFloat()
    return hitPercent < 0.8f
}

fun Structure.getRepairWidget(): Float {
    val structure = this
    val hits: Int = structure.hits
    val maxHits: Int = structure.hitsMax
    val hitPercent: Float = hits / maxHits.toFloat()
    return hitPercent
}
