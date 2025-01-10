package screeps.api.nest

import screeps.api.BodyPartConstant
import screeps.api.CARRY
import screeps.api.MOVE
import screeps.api.WORK
import screeps.api.entity.RoomInfo
import screeps.api.roles.CreepRole

class MaintainerCreepNest(
    creepNest: CreepSpawnHandler?,
    roomInfo: RoomInfo
) : AbstractCreepNest(
    creepNest,
    roomInfo
) {
    override val TAG: String = "MaintainerCreepNest"

    private val body: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)

    private val minCount = 1

    override fun handle(): Boolean {
        val creepNestResult = creepNest?.handle() ?: false
        if (creepNestResult) {
            return true
        }
        return spawnBuilder()
    }

    private fun spawnBuilder(): Boolean {
        val currentHarvesterCount = roomInfo.roomCreepInfo.harvesterList.size
        return spawnCreep(
            count = (minCount - currentHarvesterCount).coerceAtLeast(0),
            role = CreepRole.Maintainer,
            body = body
        )
    }
}
