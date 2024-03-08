package screeps.sdk.extensions

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.FIND_MY_CONSTRUCTION_SITES
import screeps.api.FIND_MY_CREEPS
import screeps.api.FIND_MY_SPAWNS
import screeps.api.FIND_MY_STRUCTURES
import screeps.api.FIND_SOURCES
import screeps.api.FIND_STRUCTURES
import screeps.api.Resource
import screeps.api.Room
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn

fun Room.findDroppedResources(): List<Resource> {
    return find(FIND_DROPPED_RESOURCES).toList()
}

/**
 * 所有属于您的建筑工地
 */
fun Room.findMyConstructionList(): List<ConstructionSite> {
    return find(FIND_MY_CONSTRUCTION_SITES).toList()
}

fun Room.findSpawnMap(): Map<String, StructureSpawn> {
    val map: MutableMap<String, StructureSpawn> = HashMap()
    find(FIND_MY_SPAWNS).forEach {
        map[it.id] = it
    }
    return map
}

fun Room.findSourceMap(): Map<String, Source> {
    val map: MutableMap<String, Source> = HashMap()
    find(FIND_SOURCES).forEach {
        map[it.id] = it
    }
    return map
}

/**
 * 获取所有自己的建筑
 */
fun Room.findMyStructures(): List<Structure> {
    return find(FIND_MY_STRUCTURES).toList()
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
    }.sortedBy { structure ->
        val hits: Int = structure.hits
        val maxHits: Int = structure.hitsMax
        val hitPercent: Float = hits / maxHits.toFloat()
        hitPercent
    }
}
