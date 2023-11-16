package screeps

import screeps.ai.brain.startRoom
import screeps.ai.roles.CreepRole
import screeps.ai.roles.getRole
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Room
import screeps.api.values
import kotlin.collections.set

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.HARVESTER to 2,
    CreepRole.TRANSPORTER to 2,
    CreepRole.MAINTAINER to 1,
    CreepRole.UPGRADER to 8,
    CreepRole.BUILDER to 2
)

fun gameLoop() {
    val startCpu = Game.cpu.tickLimit
    console.log("$startCpu CPU available on tick ${Game.time}")
    // delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    val creepsByRoomAndRole: Map<CreepRole, Map<Room, List<Creep>>> = getCreepsByRole()

    startRoom(creepsByRoleAndRoom = creepsByRoomAndRole)

    console.log("Used ${startCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
}

fun getCreepsByRole(): Map<CreepRole, Map<Room, List<Creep>>> {

    val creepsByRoleAndRoom = HashMap<CreepRole, Map<Room, List<Creep>>>()
    Game.creeps.values.groupBy {
        it.getRole()
    }.forEach {
        creepsByRoleAndRoom[it.key] = it.value.groupBy { creep -> creep.room }
    }

    return creepsByRoleAndRoom
}
