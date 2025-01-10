package screeps.api.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_FOUND
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_CONTAINER
import screeps.api.STRUCTURE_ROAD
import screeps.api.ScreepsReturnCode
import screeps.api.entity.RoomInfo
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.findDroppedResources
import kotlin.math.abs

val MAINTENANCE_REQUIRED_BUILDING_TYPES = setOf(
    STRUCTURE_ROAD,
    STRUCTURE_CONTAINER,
)

fun getState(state: Int): CreepState {
    // TODO: Set up a map so this is faster/better
    return CreepState.values().firstOrNull { it.ordinal == state } ?: CreepState.GetEnergy
}

abstract class AbstractRole(
    val creepList: List<Creep>,
    val roomInfo: RoomInfo
) {
    companion object {

        private const val TAG = "AbstractRole"
    }

    protected fun pickupEnergy(creep: Creep): ScreepsReturnCode {
        // TODO: Handle priority
        val energySource = creep.room.findDroppedResources().filter { it.resourceType == RESOURCE_ENERGY }
            .minByOrNull {
                (abs(creep.pos.x - it.pos.x) + abs(creep.pos.y - it.pos.y)).toFloat() / it.amount.toFloat()
            }

        if (energySource == null || energySource.amount < 10) {
            creep.say("No energy available!")
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

    abstract fun startWork()
}
