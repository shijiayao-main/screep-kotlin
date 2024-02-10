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
import screeps.sdk.extensions.findMyConstruction
import screeps.sdk.extensions.findMyCreeps
import screeps.sdk.extensions.findNeedRepairPublicBuild
import screeps.sdk.extensions.findNeedRepairSelfBuild
import screeps.sdk.extensions.findSource
import screeps.sdk.extensions.findSpawn
import screeps.sdk.extensions.findTower

fun getGameData(): List<RoomInfo>{
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
    val spawnList = room.findSpawn()
    val sourceList = room.findSource()
    val controller: StructureController? = room.controller

    val myConstructionList = room.findMyConstruction()
    val needRepairSelfBuild = room.findNeedRepairSelfBuild()
    val needRepairPublicBuild = room.findNeedRepairPublicBuild()

    val towerList = room.findTower()

    return RoomStructureInfo(
        controller = controller,
        spawnList = spawnList,
        sourceList = sourceList,
        myConstructionList = myConstructionList,
        selfNeedRepairBuildList = needRepairSelfBuild,
        publicNeedRepairBuildList = needRepairPublicBuild,
        towerList = towerList
    )
}

private fun getCreepData(room: Room): RoomCreepInfo {
    val harvesterList: MutableList<Creep> = ArrayList()
    val builderList: MutableList<Creep> = ArrayList()
    val upgraderList: MutableList<Creep> = ArrayList()
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
            CreepRole.Updater -> upgraderList.add(creep)
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
        updaterList = upgraderList,
        transporterList = transporterList,
        maintainerList = maintainerList,
        defenderList = defenderList,
        enemyList = enemyList,
    )
}