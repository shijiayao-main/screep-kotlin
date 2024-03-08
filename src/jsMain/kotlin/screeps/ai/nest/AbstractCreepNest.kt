package screeps.ai.nest

import screeps.ai.entity.RoomInfo
import screeps.ai.roles.CreepRole
import screeps.api.BodyPartConstant
import screeps.api.ERR_BUSY
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.Game
import screeps.api.OK
import screeps.api.structures.StructureSpawn
import screeps.sdk.ScreepsLog

abstract class AbstractCreepNest(
    val creepNest: CreepSpawnHandler?,
    val roomInfo: RoomInfo
) : CreepSpawnHandler {

    abstract val TAG: String

    protected fun spawnCreep(
        count: Int,
        role: CreepRole,
        body: Array<BodyPartConstant>
    ): Boolean {
        var needSpawnCount = count
        if (count <= 0) {
            return false
        }
        val spawnMap = roomInfo.roomStructureInfo.spawnMap
        spawnMap.forEach {
            val spawn = it.value
            val result = spawnCreep(
                spawn = spawn,
                role = role,
                body = body
            )
            if (result) {
                needSpawnCount -= 1
                if (needSpawnCount <= 0) {
                    return true
                }
            }
        }
        return true
    }

    private fun spawnCreep(
        spawn: StructureSpawn,
        role: CreepRole,
        body: Array<BodyPartConstant>
    ): Boolean {
        val creepName = "${role.namePrefix}_${Game.time}"
        when (val code = spawn.spawnCreep(body, creepName)) {
            OK -> {
                ScreepsLog.d(TAG, "spawning $creepName with body $body")
                return true
            }

            ERR_BUSY -> {
                ScreepsLog.d(TAG, "Spawner $spawn in ${spawn.room} is busy")
                return false
            }

            ERR_NOT_ENOUGH_ENERGY -> {
                ScreepsLog.d(TAG, "Not enough energy to spawn a new ${role.name}")
                return false
            }

            else -> {
                ScreepsLog.d(TAG, "Unhandled error code $code")
                return false
            }
        }
    }
}
