package screeps.sdk.extensions

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_MY_CONSTRUCTION_SITES
import screeps.api.FIND_MY_CREEPS
import screeps.api.FIND_MY_SPAWNS
import screeps.api.FIND_MY_STRUCTURES
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Room
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower

fun Room.findMyConstruction(): List<ConstructionSite> {
    return find(FIND_MY_CONSTRUCTION_SITES).toList()
}

fun Room.findSpawn(): List<StructureSpawn> {
    return find(FIND_MY_SPAWNS).toList()
}

fun Room.findSource(): List<Source> {
    return find(FIND_SOURCES).toList()
}

/**
 * structureType == STRUCTURE_TOWER
 */
fun Room.findTower(): List<StructureTower> {
    return find(FIND_MY_STRUCTURES).filterIsInstance<StructureTower>()
}

fun Room.findMyCreeps(): List<Creep> {
    return find(FIND_MY_CREEPS).toList()
}

fun Room.findEnemy(): List<Creep> {
    return find(FIND_HOSTILE_CREEPS).toList()
}

/**
 * 找出需要修复的公共建筑(墙壁, 道路, 堡垒), 且生命值小于80%
 */
fun Room.findNeedRepairPublicBuild(): List<Structure> {
    return find(FIND_STRUCTURES).filter { structure ->
        val hits: Int = structure.hits
        val maxHits: Int = structure.hitsMax
        val hitPercent: Float = hits / maxHits.toFloat()
        structure.isPublicBuild() && hitPercent < 0.8f
    }
}

/**
 * 找出需要修复的个人建筑, 且生命值小于80%
 */
fun Room.findNeedRepairSelfBuild(): List<Structure> {
    return find(FIND_MY_STRUCTURES).filter { structure: Structure ->
        val hits: Int = structure.hits
        val maxHits: Int = structure.hitsMax
        val hitPercent: Float = hits / maxHits.toFloat()
        hitPercent < 0.8f
    }
}
