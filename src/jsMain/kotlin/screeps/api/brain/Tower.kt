package screeps.api.brain

import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.entity.RoomInfo
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.isPublicBuild
import kotlin.math.abs

private const val TAG = "Tower"

fun startTower(roomInfo: RoomInfo) {
    val towerList = roomInfo.roomStructureInfo.towerMap
    val enemyList = roomInfo.roomCreepInfo.enemyList
    val selfNeedRepairBuildList = roomInfo.roomStructureInfo.selfNeedRepairBuildList
    val publicNeedRepairBuildList = roomInfo.roomStructureInfo.publicNeedRepairBuildList

    towerList.forEach { data ->
        val tower = data.value
        runTower(
            tower = tower,
            enemyList = enemyList,
            selfNeedRepairBuildList = selfNeedRepairBuildList,
            publicNeedRepairBuildList = publicNeedRepairBuildList,
        )
    }
}

private fun runTower(
    tower: StructureTower,
    enemyList: List<Creep>,
    selfNeedRepairBuildList: List<Structure>,
    publicNeedRepairBuildList: List<Structure>,
) {
    if (enemyList.isNotEmpty()) {
        attackEnemy(tower = tower, enemyList = enemyList)
        return
    }

    // 获取当前的容量
    val energy: Int = tower.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0

    // todo: heal creep

    if (energy < 500) {
        return
    }

    if (selfNeedRepairBuildList.isNotEmpty()) {
        val result = repairBuild(tower = tower, buildList = selfNeedRepairBuildList)
        if (result) {
            // 修了建筑就不要做别的了
            return
        }
    }

    if (energy < 800) {
        return
    }
    if (publicNeedRepairBuildList.isNotEmpty()) {
        repairBuild(tower = tower, buildList = publicNeedRepairBuildList)
    }
}

private fun attackEnemy(
    tower: StructureTower,
    enemyList: List<Creep>
) {
    val nearestCreep: Creep = enemyList.minByOrNull { creep ->
        // 按照距离进行排序, 先去打近的
        abs(creep.pos.x - tower.pos.x) + abs(creep.pos.y - tower.pos.y)
    } ?: return

    when (val result = tower.attack(nearestCreep)) {
        OK -> {
            ScreepsLog.d(TAG, "$tower attacking $nearestCreep")
        }

        ERR_NOT_ENOUGH_ENERGY -> {
            ScreepsLog.d(TAG, "$tower failed to attack $nearestCreep: out of energy")
        }

        else -> {
            ScreepsLog.d(TAG, "$tower failed to attack $nearestCreep: $result")
        }
    }
}

private fun repairBuild(
    tower: StructureTower,
    buildList: List<Structure>
): Boolean {
    val target: Structure = buildList.minByOrNull { structure ->
        val hits: Int = structure.hits
        val maxHits: Int = structure.hitsMax
        val hitPercent: Float = hits / maxHits.toFloat()
        hitPercent
    } ?: return false

    val hitPercent: Float = target.hits / target.hitsMax.toFloat()
    if (hitPercent > 0.8) {
        return false
    }

    if (target.isPublicBuild() && target.hits > 500000) {
        return false
    }

    when (val code = tower.repair(target)) {
        OK -> {
            return true
        }

        ERR_NOT_ENOUGH_ENERGY -> {
            ScreepsLog.d(TAG, "$tower failed to repair build: $target, out of energy")
            return false
        }

        else -> {
            ScreepsLog.d(TAG, "$tower failed to repair build: $target, code: $code")
            return false
        }
    }
}
