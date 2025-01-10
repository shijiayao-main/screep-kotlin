package screeps.api.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.entity.RoomInfo

class DefenderRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo,
) {
    override fun startWork() {
    }

    private fun run(creep: Creep) {
        val hostileCreep: Creep? = creep.room.find(FIND_HOSTILE_CREEPS).firstOrNull()
        if (hostileCreep == null) {
            creep.say("💤 Idle")
            return
        }
        if (creep.attack(hostileCreep) == ERR_NOT_IN_RANGE) {
            creep.moveTo(hostileCreep)
        }
        creep.say("🔫 Attacking")
    }
}
