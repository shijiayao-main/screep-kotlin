package screeps.api.brain

import screeps.api.entity.RoomCreepInfo
import screeps.api.entity.RoomInfo
import screeps.api.roles.BuilderRole
import screeps.api.roles.DefenderRole
import screeps.api.roles.HarvesterRole
import screeps.api.roles.MaintainerRole
import screeps.api.roles.TransporterRole
import screeps.api.roles.UpdaterRole

private const val TAG = "Room"

fun startRoom(roomInfo: RoomInfo) {
    startTower(roomInfo = roomInfo)
    runSpawn(roomInfo = roomInfo)
    runCreep(roomInfo = roomInfo, creepInfo = roomInfo.roomCreepInfo)
}

private fun runCreep(roomInfo: RoomInfo, creepInfo: RoomCreepInfo) {
    listOf(
        HarvesterRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
        UpdaterRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
        BuilderRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
        TransporterRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
        MaintainerRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
        DefenderRole(creepList = creepInfo.harvesterList, roomInfo = roomInfo),
    ).forEach {
        it.startWork()
    }
}

// private fun claimNewRooms(creepsByRoomAndRole: Map<CreepRole, Map<Room, List<Creep>>>) {
//    val nextRoomFlag = Game.flags["NextRoom"] ?: return ScreepsLog.d(TAG, "No NextRoom flag, skipping claim room routine")
//
//    if (nextRoomFlag.memory.complete) {
//        ScreepsLog.d(TAG, "Current room claim target has been successfully claimed")
//        if (Memory.flags?.get(nextRoomFlag.name) != null) {
//            delete(Memory.flags!![nextRoomFlag.name])
//        }
//        nextRoomFlag.remove()
//        return
//    }
//    if (nextRoomFlag.memory.spawnerId == null) {
//        creepsByRoomAndRole[CreepRole.Claimer]?.flatMap { it.value }?.firstOrNull()
//            ?: spawnCrossRoomCreep(
//                CreepRole.Claimer, nextRoomFlag
//            )
//            ?: return ScreepsLog.d(TAG, "No claimer creep could be located or created")
//    } else {
//        val builders =
//            creepsByRoomAndRole[CreepRole.RemoteConstruction]?.flatMap { it.value }?.toMutableList() ?: mutableListOf()
//
//        if (builders.size < 2) {
//            spawnCrossRoomCreep(
//                CreepRole.RemoteConstruction, nextRoomFlag
//            ) ?: return ScreepsLog.d(TAG, "No RCV could be located or created")
//        }
//    }
// }
