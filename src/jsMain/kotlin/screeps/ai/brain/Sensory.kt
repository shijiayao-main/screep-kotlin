package screeps.ai.brain

import screeps.ai.entity.RoomCreepInfo
import screeps.ai.entity.RoomInfo
import screeps.ai.entity.RoomStructureInfo
import screeps.ai.roles.CreepRole
import screeps.ai.roles.getCreepRoleByName
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Room
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureController
import screeps.api.structures.StructureExtension
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureTower
import screeps.api.values
import screeps.sdk.extensions.findEnemy
import screeps.sdk.extensions.findMyConstructionList
import screeps.sdk.extensions.findMyCreeps
import screeps.sdk.extensions.findMyStructures
import screeps.sdk.extensions.findNeedRepairPublicBuild
import screeps.sdk.extensions.findSourceMap
import screeps.sdk.extensions.findSpawnMap
import screeps.sdk.extensions.getRepairWidget
import screeps.sdk.extensions.isNeedRepair

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

    val myStructureList = room.findMyStructures()
    val myStructureMap: MutableMap<String, Structure> = HashMap()
    val towerList: MutableMap<String, StructureTower> = HashMap()
    val extensionStructureList: MutableList<StructureExtension> = ArrayList()
    val linkStructureList: MutableList<StructureLink> = ArrayList()
    val containerStructureList: MutableList<StructureContainer> = ArrayList()

    val needRepairSelfBuildList: MutableList<Structure> = ArrayList()
    myStructureList.forEach {
        val key = it.id
        myStructureMap[key] = it
        when (it) {
            is StructureTower -> {
                towerList[key] = it
            }

            is StructureExtension -> {
                extensionStructureList.add(it)
            }

            is StructureLink -> {
                linkStructureList.add(it)
            }

            is StructureContainer -> {
                containerStructureList.add(it)
            }
        }

        if (it.isNeedRepair()) {
            needRepairSelfBuildList.add(it)
        }
    }

    val myConstructionMap = room.findMyConstructionList()

    val needRepairPublicBuild = room.findNeedRepairPublicBuild()

    val structureStorage = room.storage

    return RoomStructureInfo(
        controller = controller,
        spawnMap = spawnMap,
        structureStorage = structureStorage,
        sourceMap = sourceMap,
        extensionStructureList = extensionStructureList,
        linkStructureList = linkStructureList,
        containerStructureList = containerStructureList,
        myStructureMap = myStructureMap,
        myConstructionList = myConstructionMap,
        selfNeedRepairBuildList = needRepairSelfBuildList.sortedBy {
            it.getRepairWidget()
        },
        publicNeedRepairBuildList = needRepairPublicBuild,
        towerMap = towerList
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
