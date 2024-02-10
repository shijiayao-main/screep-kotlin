package screeps.ai.brain

import screeps.ai.entity.RoomCreepInfo
import screeps.ai.entity.RoomInfo
import screeps.ai.roles.AbstractRole
import screeps.ai.roles.CreepRole

private const val TAG = "Room"

fun startRoom(roomInfo: RoomInfo) {
    startTower(roomInfo = roomInfo)
    runSpawn(roomInfo = roomInfo)
//    val creepsByRole = HashMap<CreepRole, List<Creep>>()
//    var minCreepsInUnfilledRole = 1000
//    for (role in CreepRole.values()) {
//        val creepsByRoom = creepsByRoleAndRoom[role] ?: hashMapOf()
//        val creeps = creepsByRoom[room] ?: emptyList()
//        creepsByRole[role] = creeps
//
//        if (creeps.size < minCreepsInUnfilledRole && creeps.size < (roleMemberCount[role] ?: 0)) {
//            ScreepsLog.d(TAG, "$role only has ${creeps.size}")
//            minCreepsInUnfilledRole = creeps.size
//        }
//    }

//    if (minCreepsInUnfilledRole != 1000) {
//        ScreepsLog.d(TAG, "Least populated unfilled role in $room only has $minCreepsInUnfilledRole creeps")
//    } else {
//        ScreepsLog.d(TAG, "All roles filled in $room")
//    }
//    var prioritySpawnActive = false
//    if ((creepsByRole[CreepRole.Harvester]?.size ?: 0) < (room.find(FIND_SOURCES).size)) {
//        ScreepsLog.d(TAG, "$room spawning priority harvester")
//        spawnNewCreep(CreepRole.Harvester, room)
//        prioritySpawnActive = true
//    } else if ((creepsByRole[CreepRole.Transporter]?.size ?: 0) < roleMemberCount[CreepRole.Transporter]) {
//        ScreepsLog.d(TAG, "$room spawning priority transporter")
//        spawnNewCreep(CreepRole.Transporter, room)
//        prioritySpawnActive = true
//    }

    runCreep(creepInfo = roomInfo.roomCreepInfo)
}

private fun runCreep(creepInfo: RoomCreepInfo) {
    creepInfo.harvesterList.forEach {
        AbstractRole.build(creepRole = CreepRole.Harvester, creep = it).run()
    }
    creepInfo.updaterList.forEach {
        AbstractRole.build(creepRole = CreepRole.Updater, creep = it).run()
    }
    creepInfo.builderList.forEach {
        AbstractRole.build(creepRole = CreepRole.Builder, creep = it).run()
    }
    creepInfo.transporterList.forEach {
        AbstractRole.build(creepRole = CreepRole.Transporter, creep = it).run()
    }
    creepInfo.maintainerList.forEach {
        AbstractRole.build(creepRole = CreepRole.Maintainer, creep = it).run()
    }
    creepInfo.defenderList.forEach {
        AbstractRole.build(creepRole = CreepRole.Defender, creep = it).run()
    }
}

// private fun runRoom(
//    room: Room,
//    creepsByRoleAndRoom: Map<CreepRole, Map<Room, List<Creep>>>
// ) {
//    startTower(room = room)
//
//    var minCreepsInUnfilledRole = 1000
//    val creepsByRole = HashMap<CreepRole, List<Creep>>()
//
//    for (role in CreepRole.values()) {
//        val creepsByRoom = creepsByRoleAndRoom[role] ?: hashMapOf()
//        val creeps = creepsByRoom[room] ?: emptyList()
//        creepsByRole[role] = creeps
//
//        if (creeps.size < minCreepsInUnfilledRole && creeps.size < (roleMemberCount[role] ?: 0)) {
//            ScreepsLog.d(TAG, "$role only has ${creeps.size}")
//            minCreepsInUnfilledRole = creeps.size
//        }
//    }
//
//    if (minCreepsInUnfilledRole != 1000) {
//        ScreepsLog.d(TAG, "Least populated unfilled role in $room only has $minCreepsInUnfilledRole creeps")
//    } else {
//        ScreepsLog.d(TAG, "All roles filled in $room")
//    }
//    var prioritySpawnActive = false
//    if ((creepsByRole[CreepRole.Harvester]?.size ?: 0) < (room.find(FIND_SOURCES).size)) {
//        ScreepsLog.d(TAG, "$room spawning priority harvester")
//        spawnNewCreep(CreepRole.Harvester, room)
//        prioritySpawnActive = true
//    } else if ((creepsByRole[CreepRole.Transporter]?.size ?: 0) < roleMemberCount[CreepRole.Transporter]) {
//        ScreepsLog.d(TAG, "$room spawning priority transporter")
//        spawnNewCreep(CreepRole.Transporter, room)
//        prioritySpawnActive = true
//    }
//
//    for (record in creepsByRole) {
//        val creepRole = record.key
//        val creeps = record.value
//        val creepCount = creeps.size
//        var maxCreepsInRole = roleMemberCount[creepRole] ?: 0
//        // If room is still missing initial extensions, limit max creeps per role
//        if (room.energyCapacityAvailable < 550 && maxCreepsInRole > 2) {
//            maxCreepsInRole = minOf(maxCreepsInRole, 2)
//            ScreepsLog.d(TAG, "$room still getting online, spawning max of $maxCreepsInRole ${creepRole.name}s")
//        }
//        ScreepsLog.d(TAG, "$room $creepRole: $creepCount/$maxCreepsInRole")
//        // Spawn more creeps if we are not at the desired volume
//        runSpawn(roomInfo = room)
//        if (creepCount < maxCreepsInRole && !prioritySpawnActive) {
//            if (creepCount <= minCreepsInUnfilledRole) {
//                spawnNewCreep(creepRole, room)
//            } else {
//                ScreepsLog.d(
//                    TAG,
//                    "Not spawning new $creepRole since there are ${creeps.size} and another role only has $minCreepsInUnfilledRole"
//                )
//            }
//        }
//
//        for (creep in creeps) {
//            try {
//                AbstractRole.build(creepRole, creep).run()
//            } catch (error: IllegalArgumentException) {
//                if (creep.body.any { it.type == WORK }) {
//                    creep.setRole(CreepRole.Updater)
//                } else {
//                    creep.setRole(CreepRole.Transporter)
//                }
//            } catch (error: Exception) {
//                ScreepsLog.d(TAG, "${creep.name} failed to run due to error: $error")
//            }
//        }
//    }
// }

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
