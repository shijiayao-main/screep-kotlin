package screeps.api.nest

import screeps.api.BodyPartConstant
import screeps.api.CARRY
import screeps.api.MOVE
import screeps.api.WORK
import screeps.api.entity.RoomInfo
import screeps.api.roles.CreepRole

class BuilderCreepNest(
    creepNest: CreepSpawnHandler?,
    roomInfo: RoomInfo
) : AbstractCreepNest(
    creepNest,
    roomInfo
) {
    override val TAG: String = "BuilderCreepNest"

    private val body: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)

    private val minCount = 2

    override fun handle(): Boolean {
        val creepNestResult = creepNest?.handle() ?: false
        if (creepNestResult) {
            return true
        }
        return spawnBuilder()
    }

    private fun spawnBuilder(): Boolean {
        // todo: 检查是否存在建筑工地, 不存在就不用创建builder

        val currentHarvesterCount = roomInfo.roomCreepInfo.harvesterList.size
        return spawnCreep(
            count = (minCount - currentHarvesterCount).coerceAtLeast(0),
            role = CreepRole.Builder,
            body = body
        )
    }
}
