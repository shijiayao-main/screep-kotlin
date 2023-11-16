package screeps.ai.brain

import screeps.ai.roles.CreepRole
import screeps.ai.roles.Role
import screeps.ai.roles.complete
import screeps.ai.roles.setRole
import screeps.ai.roles.spawnerId
import screeps.api.Creep
import screeps.api.FIND_SOURCES
import screeps.api.Game
import screeps.api.Memory
import screeps.api.Room
import screeps.api.WORK
import screeps.api.compareTo
import screeps.api.get
import screeps.api.values
import screeps.roleMemberCount
import screeps.sdk.ScreepsLog
import screeps.spawnCrossRoomCreep
import screeps.spawnNewCreep
import screeps.utils.unsafe.delete
import kotlin.collections.set

private const val TAG = "Room"

fun startRoom(creepsByRoleAndRoom: Map<CreepRole, Map<Room, List<Creep>>>) {
    for (room in Game.rooms.values) {
        val roomStartCpu = Game.cpu.tickLimit

        runRoom(
            room = room,
            creepsByRoleAndRoom = creepsByRoleAndRoom
        )
        ScreepsLog.d(TAG, "$room used ${roomStartCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
    }
    claimNewRooms(creepsByRoomAndRole = creepsByRoleAndRoom)
}

private fun runRoom(
    room: Room,
    creepsByRoleAndRoom: Map<CreepRole, Map<Room, List<Creep>>>
) {
    startTower(room = room)

    var minCreepsInUnfilledRole = 1000
    val creepsByRole = HashMap<CreepRole, List<Creep>>()

    for (role in CreepRole.values()) {
        val creepsByRoom = creepsByRoleAndRoom[role] ?: hashMapOf()
        val creeps = creepsByRoom[room] ?: emptyList()
        creepsByRole[role] = creeps

        if (creeps.size < minCreepsInUnfilledRole && creeps.size < (roleMemberCount[role] ?: 0)) {
            ScreepsLog.d(TAG, "$role only has ${creeps.size}")
            minCreepsInUnfilledRole = creeps.size
        }
    }

    if (minCreepsInUnfilledRole != 1000) {
        ScreepsLog.d(TAG, "Least populated unfilled role in $room only has $minCreepsInUnfilledRole creeps")
    } else {
        ScreepsLog.d(TAG, "All roles filled in $room")
    }
    var prioritySpawnActive = false
    if ((creepsByRole[CreepRole.HARVESTER]?.size ?: 0) < (room.find(FIND_SOURCES).size)) {
        ScreepsLog.d(TAG, "$room spawning priority harvester")
        spawnNewCreep(CreepRole.HARVESTER, room)
        prioritySpawnActive = true
    } else if ((creepsByRole[CreepRole.TRANSPORTER]?.size ?: 0) < roleMemberCount[CreepRole.TRANSPORTER]) {
        ScreepsLog.d(TAG, "$room spawning priority transporter")
        spawnNewCreep(CreepRole.TRANSPORTER, room)
        prioritySpawnActive = true
    }

    for (record in creepsByRole) {
        val creepRole = record.key
        val creeps = record.value
        val creepCount = creeps.size
        var maxCreepsInRole = roleMemberCount[creepRole] ?: 0
        // If room is still missing initial extensions, limit max creeps per role
        if (room.energyCapacityAvailable < 550 && maxCreepsInRole > 2) {
            maxCreepsInRole = minOf(maxCreepsInRole, 2)
            ScreepsLog.d(TAG, "$room still getting online, spawning max of $maxCreepsInRole ${creepRole.name}s")
        }
        ScreepsLog.d(TAG, "$room $creepRole: $creepCount/$maxCreepsInRole")
        // Spawn more creeps if we are not at the desired volume
        if (creepCount < maxCreepsInRole && !prioritySpawnActive) {
            if (creepCount <= minCreepsInUnfilledRole) {
                spawnNewCreep(creepRole, room)
            } else {
                ScreepsLog.d(
                    TAG,
                    "Not spawning new $creepRole since there are ${creeps.size} and another role only has $minCreepsInUnfilledRole"
                )
            }
        }

        for (creep in creeps) {
            try {
                Role.build(creepRole, creep).run()
            } catch (error: IllegalArgumentException) {
                if (creep.body.any { it.type == WORK }) {
                    creep.setRole(CreepRole.UPGRADER)
                } else {
                    creep.setRole(CreepRole.TRANSPORTER)
                }
            } catch (error: Exception) {
                ScreepsLog.d(TAG, "${creep.name} failed to run due to error: $error")
            }
        }
    }
}

private fun claimNewRooms(creepsByRoomAndRole: Map<CreepRole, Map<Room, List<Creep>>>) {
    val nextRoomFlag = Game.flags["NextRoom"] ?: return ScreepsLog.d(TAG, "No NextRoom flag, skipping claim room routine")

    if (nextRoomFlag.memory.complete) {
        ScreepsLog.d(TAG, "Current room claim target has been successfully claimed")
        if (Memory.flags?.get(nextRoomFlag.name) != null) {
            delete(Memory.flags!![nextRoomFlag.name])
        }
        nextRoomFlag.remove()
        return
    }
    if (nextRoomFlag.memory.spawnerId == null) {
        creepsByRoomAndRole[CreepRole.CLAIMER]?.flatMap { it.value }?.firstOrNull()
            ?: spawnCrossRoomCreep(
                CreepRole.CLAIMER, nextRoomFlag
            )
            ?: return ScreepsLog.d(TAG, "No claimer creep could be located or created")
    } else {
        val builders =
            creepsByRoomAndRole[CreepRole.REMOTE_CONSTRUCTION]?.flatMap { it.value }?.toMutableList() ?: mutableListOf()

        if (builders.size < 2) {
            spawnCrossRoomCreep(
                CreepRole.REMOTE_CONSTRUCTION, nextRoomFlag
            ) ?: return ScreepsLog.d(TAG, "No RCV could be located or created")
        }
    }
}
