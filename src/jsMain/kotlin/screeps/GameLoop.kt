package screeps

import screeps.ai.brain.getGameData
import screeps.ai.brain.startRoom
import screeps.ai.roles.CreepRole
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Memory
import screeps.api.Record
import screeps.api.component1
import screeps.api.component2
import screeps.api.get
import screeps.api.iterator
import screeps.sdk.ScreepsLog
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.Harvester to 4,
    CreepRole.Transporter to 2,
    CreepRole.Maintainer to 1,
    CreepRole.Updater to 2,
    CreepRole.Builder to 2
)

fun gameLoop() {
    val startCpu = Game.cpu.tickLimit
    ScreepsLog.d("GameLoop", "$startCpu CPU available on tick ${Game.time}")
    // delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    val roomInfoList = getGameData()

    roomInfoList.forEach { roomInfo ->
        startRoom(roomInfo = roomInfo)
    }

    ScreepsLog.d("GameLoop", "Used ${startCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
}

fun houseKeeping(creeps: Record<String, Creep>) {
    if (Game.creeps.isEmpty()) return // this is needed because Memory.creeps is undefined

    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obsolete memory entry for creep $creepName")
            delete(Memory.creeps[creepName])
        }
    }
}