package screeps.ai.brain

import screeps.ai.entity.RoomInfo
import screeps.ai.nest.BuilderCreepNest
import screeps.ai.nest.DefenderCreepNest
import screeps.ai.nest.HarvesterCreepNest
import screeps.ai.nest.MaintainerCreepNest
import screeps.ai.nest.TransporterCreepNest
import screeps.ai.nest.UpdaterCreepNest

// val BASE_BODY = Body(arrayOf(WORK, MOVE, CARRY))
//
// val HARVESTER_BODIES = arrayOf(
//    Body(arrayOf(WORK, WORK, MOVE)),
//    Body(arrayOf(WORK, WORK, WORK, WORK, WORK, MOVE)),
// )
//
// val UPGRADE_BODIES = arrayOf(
//    Body(arrayOf(WORK, MOVE, MOVE, CARRY, CARRY)),
//    Body(arrayOf(MOVE, WORK, WORK, WORK, CARRY, CARRY, CARRY, CARRY))
// )
//
// val DEFENDER_BODIES = arrayOf(
//    Body(arrayOf(ATTACK, ATTACK, MOVE, MOVE))
// )
//
// val TRANSPORTER_BODIES = arrayOf(
//    Body(arrayOf(MOVE, MOVE, CARRY, CARRY, CARRY, CARRY)),
//    Body(arrayOf(MOVE, MOVE, CARRY, CARRY, CARRY, CARRY, CARRY, CARRY, CARRY, CARRY))
// )
//
// val BUILDER_BODIES = arrayOf(
//    Body(arrayOf(WORK, CARRY, CARRY, CARRY, MOVE)),
//    Body(arrayOf(MOVE, MOVE, WORK, WORK, CARRY, CARRY, CARRY, CARRY, CARRY))
// )
//
// val CLAIMER_BODIES = arrayOf(
//    Body(arrayOf(MOVE, CLAIM)),
// )
//
// val REMOTE_CONSTRUCTION_BODIES = arrayOf(
//    Body(
//        arrayOf(
//            MOVE, MOVE, MOVE, MOVE, MOVE,
//            MOVE, MOVE, MOVE, MOVE, MOVE,
//            MOVE, MOVE, MOVE, MOVE, MOVE,
//            MOVE, MOVE, MOVE, MOVE, MOVE,
//            WORK, WORK, WORK, WORK, WORK,
//            WORK, WORK, WORK, WORK, WORK,
//            CARRY, CARRY, CARRY, CARRY, CARRY,
//            CARRY, CARRY, CARRY, CARRY, CARRY,
//            CARRY, CARRY, CARRY, CARRY, CARRY,
//            CARRY, CARRY, CARRY, CARRY, CARRY
//        )
//    ),
// )

// fun getBody(role: CreepRole, energyAvailable: Int): Body {
//    val bodies = when (role) {
//        CreepRole.Unassigned -> return BASE_BODY
//        CreepRole.Harvester -> HARVESTER_BODIES
//        CreepRole.Upgrader -> UPGRADE_BODIES
//        CreepRole.Transporter -> TRANSPORTER_BODIES
//        CreepRole.Builder -> BUILDER_BODIES
//        CreepRole.Maintainer -> BUILDER_BODIES
//        CreepRole.Claimer -> CLAIMER_BODIES
//        CreepRole.RemoteConstruction -> REMOTE_CONSTRUCTION_BODIES
//        CreepRole.Defender -> DEFENDER_BODIES
//    }
//
//    return bodies.last { it.cost <= energyAvailable }
// }

// fun spawnCreep(spawn: StructureSpawn, role: CreepRole, body: Body): Creep? {
//    val newName = "creep_${role.name}_${Game.time}"
//    val code = spawn.spawnCreep(body.parts, newName)
//    when (code) {
//        OK -> console.log("spawning $newName with body ${body.parts}")
//        ERR_BUSY -> console.log("Spawner $spawn in ${spawn.room} is busy")
//        ERR_NOT_ENOUGH_ENERGY -> console.log("Not enough energy to spawn a new ${role.name}")
//        else -> console.log("Unhandled error code $code")
//    }
//
//    if (code != OK) {
//        return null
//    }
//
//    val creep = Game.creeps[newName]!!
//    creep.setRole(role)
//
//    return creep
// }
//
// fun spawnNewCreep(
//    role: CreepRole,
//    room: Room
// ): Creep? {
//
//    val body = try {
//        getBody(role, room.energyAvailable)
//    } catch (error: NoSuchElementException) {
//        console.log("Couldn't determine body for $role in $room with ${room.energyAvailable} energy")
//        return null
//    }
//
//    val spawns = room.find(FIND_MY_SPAWNS)
//
//    for (spawn in spawns) {
//        val creep = spawnCreep(spawn, role, body)
//        if (creep != null) {
//            return creep
//        }
//    }
//    console.log("Unable to spawn new $role creep in $room")
//    return null
// }
//
// fun spawnCrossRoomCreep(role: CreepRole, targetFlag: Flag): Creep? {
//    val body = when (role) {
//        CreepRole.Claimer -> CLAIMER_BODIES[0]
//        CreepRole.RemoteConstruction -> REMOTE_CONSTRUCTION_BODIES[0]
//        else -> throw IllegalArgumentException("$role not supported yet")
//    }
//
//    val spawner = Game.spawns.values.filter { it.room.energyAvailable > body.cost }.minByOrNull {
//        it.pos.getRangeTo(targetFlag)
//    }
//
//    if (spawner == null) {
//        console.log("No spawners available to create a new $role!")
//        return null
//    }
//
//    return spawnCreep(spawner, role, body)
// }

fun runSpawn(roomInfo: RoomInfo) {
    val harvesterCreepNest = HarvesterCreepNest(creepNest = null, roomInfo = roomInfo)
    val updaterCreepNest = UpdaterCreepNest(creepNest = harvesterCreepNest, roomInfo = roomInfo)
    val builderCreepNest = BuilderCreepNest(creepNest = updaterCreepNest, roomInfo = roomInfo)
    val transporterCreepNest = TransporterCreepNest(creepNest = builderCreepNest, roomInfo = roomInfo)
    val maintainerCreepNest = MaintainerCreepNest(creepNest = transporterCreepNest, roomInfo = roomInfo)
    val defenderCreepNest = DefenderCreepNest(creepNest = maintainerCreepNest, roomInfo = roomInfo)
    defenderCreepNest.handle()
}
