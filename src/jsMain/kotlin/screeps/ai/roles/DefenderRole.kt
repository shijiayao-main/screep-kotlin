package screeps.ai.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_HOSTILE_CREEPS

class DefenderRole(creep: Creep) : AbstractRole(creep) {
    override fun run() {
        val hostileCreep: Creep? = creep.room.find(FIND_HOSTILE_CREEPS).firstOrNull()
        if (hostileCreep == null) {
            say("💤 Idle")
            return
        }
        if (creep.attack(hostileCreep) == ERR_NOT_IN_RANGE) {
            creep.moveTo(hostileCreep)
        }
        say("🔫 Attacking")
    }
}
