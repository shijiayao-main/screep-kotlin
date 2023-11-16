package screeps.ai.brain

import screeps.api.FIND_MY_STRUCTURES
import screeps.api.Room
import screeps.api.STRUCTURE_TOWER
import screeps.api.structures.StructureTower
import screeps.runTower

fun startTower(room: Room) {
    room.find(FIND_MY_STRUCTURES).filter {
        it.structureType == STRUCTURE_TOWER
    }.map {
        it as StructureTower
    }.forEach {
        runTower(it)
    }
}