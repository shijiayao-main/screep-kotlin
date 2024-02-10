package screeps.ai.roles

import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_NOT_FOUND
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_ROAD
import screeps.api.ScreepsReturnCode
import screeps.sdk.ScreepsLog
import screeps.utils.memory.memory
import kotlin.math.abs

var CreepMemory.state: Int by memory { CreepState.GET_ENERGY.ordinal }
var CreepMemory.role: Int by memory { CreepRole.Unassigned.ordinal }

val MAINTENANCE_REQUIRED_BUILDING_TYPES = setOf(
    STRUCTURE_ROAD,
    STRUCTURE_CONTAINER,
)

fun getState(state: Int): CreepState {
    // TODO: Set up a map so this is faster/better
    return CreepState.values().firstOrNull { it.ordinal == state } ?: CreepState.GET_ENERGY
}

abstract class AbstractRole(val creep: Creep) {
    companion object {

        private const val TAG = "AbstractRole"

        /*
            Instantiate concrete subclass based on given role and creep
         */
        fun build(creepRole: CreepRole, creep: Creep): AbstractRole {
            return when (creepRole) {
                CreepRole.Unassigned -> throw IllegalArgumentException("Cannot process a creep without a role")
                CreepRole.Harvester -> HarvesterRole(creep)
                CreepRole.Updater -> UpgraderRole(creep)
                CreepRole.Transporter -> TransporterRole(creep)
                CreepRole.Builder -> BuilderRole(creep)
                CreepRole.Maintainer -> MaintainerRole(creep)
                CreepRole.Claimer -> ClaimerRole(creep)
                CreepRole.RemoteConstruction -> RemoteConstructionVehicleRole(creep)
                CreepRole.Defender -> DefenderRole(creep)
            }
        }
    }

    var state: CreepState = getState(creep.memory.state)
        set(value) {
            field = value
            creep.memory.state = value.ordinal
        }

    protected fun say(message: String) {
        if (message.isNotBlank()) {
            creep.say(message)
        }
    }

    protected fun pickupEnergy(): ScreepsReturnCode {
        // TODO: Handle priority
        val energySource = creep.room.find(FIND_DROPPED_RESOURCES).filter { it.resourceType == RESOURCE_ENERGY }
            .minByOrNull {
                (abs(creep.pos.x - it.pos.x) + abs(creep.pos.y - it.pos.y)).toFloat() / it.amount.toFloat()
            }

        if (energySource == null || energySource.amount < 10) {
            say("No energy available!")
            return ERR_NOT_FOUND
        }

        val status = creep.pickup(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        } else if (status != OK) {
            ScreepsLog.d(TAG, "Gather failed with code $status")
        }
        return status
    }

    abstract fun run()
}

// setRole and setRole are needed before any Role instances are set up
// So set them as methods on the creep itself
fun Creep.setRole(newRole: CreepRole) {
    this.memory.role = newRole.ordinal
}

fun Creep.getRole(): CreepRole {
    // TODO: Set up a map so this is faster/better
    return CreepRole.values().firstOrNull { it.ordinal == memory.role } ?: CreepRole.Unassigned
}
