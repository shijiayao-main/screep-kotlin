package screeps.api.nest

import screeps.api.BodyPartConstant
import screeps.api.CARRY
import screeps.api.MOVE
import screeps.api.WORK
import screeps.api.entity.RoomInfo
import screeps.api.roles.CreepRole

class TransporterCreepNest(
    creepNest: CreepSpawnHandler?,
    roomInfo: RoomInfo
) : AbstractCreepNest(
    creepNest,
    roomInfo
) {
    override val TAG: String = "TransporterCreepNest"

    private val body: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)

    private val minCount = 2

    override fun handle(): Boolean {
        val creepNestResult = creepNest?.handle() ?: false
        if (creepNestResult) {
            return true
        }
        return spawnTransporter()
    }

    private fun spawnTransporter(): Boolean {
        val currentHarvesterCount = roomInfo.roomCreepInfo.harvesterList.size
        return spawnCreep(
            count = (minCount - currentHarvesterCount).coerceAtLeast(0),
            role = CreepRole.Transporter,
            body = body
        )
    }
}
