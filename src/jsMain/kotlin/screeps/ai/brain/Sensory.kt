package screeps.ai.brain

import screeps.ai.entity.RoomCreepInfo
import screeps.ai.entity.RoomInfo
import screeps.ai.entity.RoomStructureInfo
import screeps.ai.roles.CreepRole
import screeps.ai.roles.getCreepRoleByName
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Room
import screeps.api.structures.StructureController
import screeps.api.values
import screeps.sdk.extensions.findEnemy
import screeps.sdk.extensions.findMyConstructionMap
import screeps.sdk.extensions.findMyCreeps
import screeps.sdk.extensions.findNeedRepairPublicBuild
import screeps.sdk.extensions.findNeedRepairSelfBuild
import screeps.sdk.extensions.findSourceMap
import screeps.sdk.extensions.findSpawnMap
import screeps.sdk.extensions.findTowerMap

fun getGameData(): List<RoomInfo> {
    val roomInfoList: MutableList<RoomInfo> = ArrayList()
    Game.rooms.values.forEach { room: Room ->
        val roomStructureInfo = getStructureData(room = room)

        val roomCreepInfo = getCreepData(room = room)
        val roomInfo = RoomInfo(
            room = room,
            roomCreepInfo = roomCreepInfo,
            roomStructureInfo = roomStructureInfo,
        )
        roomInfoList.add(roomInfo)
    }
    return roomInfoList
}

private fun getStructureData(room: Room): RoomStructureInfo {
    val spawnMap = room.findSpawnMap()
    val sourceMap = room.findSourceMap()
    val controller: StructureController? = room.controller

    val myConstructionMap = room.findMyConstructionMap()
    val needRepairSelfBuild = room.findNeedRepairSelfBuild()
    val needRepairPublicBuild = room.findNeedRepairPublicBuild()

    val towerMap = room.findTowerMap()

    return RoomStructureInfo(
        controller = controller,
        spawnMap = spawnMap,
        sourceMap = sourceMap,
        myConstructionMap = myConstructionMap,
        selfNeedRepairBuildList = needRepairSelfBuild,
        publicNeedRepairBuildList = needRepairPublicBuild,
        towerMap = towerMap
    )
}

private fun getCreepData(room: Room): RoomCreepInfo {
    val harvesterList: MutableList<Creep> = ArrayList()
    val builderList: MutableList<Creep> = ArrayList()
    val updaterList: MutableList<Creep> = ArrayList()
    val transporterList: MutableList<Creep> = ArrayList()
    val maintainerList: MutableList<Creep> = ArrayList()
    val defenderList: MutableList<Creep> = ArrayList()

    val enemyList: List<Creep> = room.findEnemy()

    val myCreepList = room.findMyCreeps()
    myCreepList.forEach { creep: Creep ->
        val creepRole = creep.name.getCreepRoleByName()
        when (creepRole) {
            CreepRole.Harvester -> harvesterList.add(creep)
            CreepRole.Transporter -> transporterList.add(creep)
            CreepRole.Updater -> updaterList.add(creep)
            CreepRole.Builder -> builderList.add(creep)
            CreepRole.Maintainer -> maintainerList.add(creep)
            CreepRole.Defender -> defenderList.add(creep)

            // 默认都是harvester, 等后面代码加了再说
            CreepRole.Claimer -> harvesterList.add(creep)
            CreepRole.RemoteConstruction -> harvesterList.add(creep)
            CreepRole.Unassigned -> harvesterList.add(creep)
        }
    }

    return RoomCreepInfo(
        harvesterList = harvesterList,
        builderList = builderList,
        updaterList = updaterList,
        transporterList = transporterList,
        maintainerList = maintainerList,
        defenderList = defenderList,
        enemyList = enemyList,
    )
}
