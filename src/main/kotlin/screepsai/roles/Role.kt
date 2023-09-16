package screepsai.roles

import screeps.api.*
import screeps.utils.memory.memory
import kotlin.math.abs

var CreepMemory.state: Int by memory { CreepState.GET_ENERGY.ordinal }
var CreepMemory.role: Int by memory { CreepRole.UNASSIGNED.ordinal }

enum class CreepRole {
    UNASSIGNED,
    HARVESTER,
    TRANSPORTER,
    UPGRADER,
    BUILDER,
    MAINTAINER,
    CLAIMER,
    REMOTE_CONSTRUCTION
}

enum class CreepState {
    GET_ENERGY,
    DO_WORK;
}

val MAINTENANCE_REQUIRED_BUILDING_TYPES = setOf(
    STRUCTURE_ROAD,
    STRUCTURE_CONTAINER,
)


fun getState(state: Int): CreepState {
    // TODO: Set up a map so this is faster/better
    return CreepState.values().firstOrNull { it.ordinal == state } ?: CreepState.GET_ENERGY
}


abstract class Role(val creep: Creep) {
    companion object {
        /*
            Instantiate concrete subclass based on given role and creep
         */
        fun build(creepRole: CreepRole, creep: Creep): Role {
            return when (creepRole) {
                CreepRole.UNASSIGNED          -> throw IllegalArgumentException("Cannot process a creep without a role")
                CreepRole.HARVESTER           -> Harvester(creep)
                CreepRole.UPGRADER            -> Upgrader(creep)
                CreepRole.TRANSPORTER         -> Transporter(creep)
                CreepRole.BUILDER             -> Builder(creep)
                CreepRole.MAINTAINER          -> Maintainer(creep)
                CreepRole.CLAIMER             -> Claimer(creep)
                CreepRole.REMOTE_CONSTRUCTION -> RemoteConstructionVehicle(creep)
            }
        }
    }

    var state: CreepState = getState(creep.memory.state)
        set(value) {
            field = value
            creep.memory.state = value.ordinal
        }

    private fun log(level: String, message: String, say: Boolean = false) {
        if (say) {
            creep.say(message)
        }
        console.log("$level ${creep.name}: $message")
    }

    fun debug(message: String, say: Boolean = false) {
        log("DEBUG", message, say = say)
    }

    fun info(message: String, say: Boolean = false) {
        log("INFO", message, say = say)
    }

    fun warning(message: String, say: Boolean = false) {
        log("WARNING", message, say = say)
    }

    fun error(message: String, say: Boolean = false) {
        log("ERROR", message, say = say)
    }

    protected fun pickupEnergy(): ScreepsReturnCode {
        // TODO: Handle priority
        val energySource = creep.room.find(FIND_DROPPED_RESOURCES).filter { it.resourceType == RESOURCE_ENERGY }
            .minByOrNull { (abs(creep.pos.x - it.pos.x) + abs(creep.pos.y - it.pos.y)).toFloat() / it.amount.toFloat() }

        if (energySource == null || energySource.amount < 10) {
            warning("No energy available!", say = true)
            return ERR_NOT_FOUND
        }

        val status = creep.pickup(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        }
        else if (status != OK) {
            error("Gather failed with code $status")
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
    return CreepRole.values().firstOrNull { it.ordinal == memory.role } ?: CreepRole.UNASSIGNED
}

