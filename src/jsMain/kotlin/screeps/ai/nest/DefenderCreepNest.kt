package screeps.ai.nest

import screeps.ai.entity.RoomInfo
import screeps.ai.roles.CreepRole
import screeps.api.ATTACK
import screeps.api.BodyPartConstant
import screeps.api.MOVE

class DefenderCreepNest(
    creepNest: CreepSpawnHandler?,
    roomInfo: RoomInfo
) : AbstractCreepNest(
    creepNest,
    roomInfo
) {
    override val TAG: String = "DefenderCreepNest"

    private val body: Array<BodyPartConstant> = arrayOf(ATTACK, MOVE)

    private val minCount = 2

    override fun handle(): Boolean {
        val creepNestResult = creepNest?.handle() ?: false
        if (creepNestResult) {
            return true
        }
        return spawnDefender()
    }

    private fun spawnDefender(): Boolean {
        val currentHarvesterCount = roomInfo.roomCreepInfo.harvesterList.size
        return spawnCreep(
            count = (minCount - currentHarvesterCount).coerceAtLeast(0),
            role = CreepRole.Builder,
            body = body
        )
    }
}