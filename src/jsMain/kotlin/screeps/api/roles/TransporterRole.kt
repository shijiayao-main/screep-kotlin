package screeps.api.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_FOUND
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_MY_STRUCTURES
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_SPAWN
import screeps.api.STRUCTURE_STORAGE
import screeps.api.STRUCTURE_TOWER
import screeps.api.StoreOwner
import screeps.api.compareTo
import screeps.api.entity.RoomInfo
import screeps.api.structures.Structure
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.setState
import screeps.sdk.extensions.tryToStoreOwner
import kotlin.math.abs

val FILLABLE_STRUCTURES = setOf(
    STRUCTURE_SPAWN,
    STRUCTURE_EXTENSION,
    STRUCTURE_TOWER,
    STRUCTURE_STORAGE
)

class TransporterRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo,
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {

    companion object {
        private const val TAG = "TransporterRole"
    }

    override fun startWork() {
        creepList.forEach { creep: Creep ->
            run(creep = creep)
        }
    }

    private fun run(creep: Creep) {
        val state = creep.getState()
        when (state) {
            CreepState.GetEnergy -> {
                getEnergy(creep = creep)
            }

            CreepState.DoWork -> {
                storeEnergy(creep = creep)
            }
        }
    }

    private fun getEnergy(creep: Creep) {
        val status = pickupEnergy(creep = creep)

        if (status == ERR_NOT_FOUND) {
            val storage = creep.room.storage
            if (storage == null || storage.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
                creep.say("No energy could be found in room")
                // Try to transport whatever energy we do have while waiting on more to be generated
                if (creep.store.getUsedCapacity(RESOURCE_ENERGY) > 50) {
                    creep.setState(CreepState.DoWork)
                }
                return
            }
            ScreepsLog.d(TAG, "No energy to pick up, gathering from storage")
            val code = creep.withdraw(storage, RESOURCE_ENERGY)
            if (code == ERR_NOT_IN_RANGE) {
                creep.moveTo(storage.pos.x, storage.pos.y)
            } else if (status != OK) {
                ScreepsLog.d(TAG, "Storage withdraw failed with code $status")
            }
        }

        if (creep.store.getFreeCapacity() == 0) {
            creep.say("Energy full")
            creep.setState(CreepState.DoWork)
        }
    }

    private fun findFillStructures(
        creep: Creep
    ): List<StoreOwner> {
        val fillableStructures = creep.room.find(FIND_MY_STRUCTURES).filter {
            it.structureType in FILLABLE_STRUCTURES
        }.mapNotNull {
            it.tryToStoreOwner()
        }.filter {
            (it.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) > 0
        }.groupBy {
            when (it.unsafeCast<Structure>().structureType) {
                // TODO: Determine priority level more intelligently
                STRUCTURE_SPAWN -> 1
                STRUCTURE_EXTENSION -> 1
                STRUCTURE_TOWER -> 2
                STRUCTURE_STORAGE -> 3
                else -> 4
            }
        }

        return fillableStructures.getOrElse(fillableStructures.keys.minOrNull() ?: 2) { emptyList() }
    }

    private fun storeEnergy(creep: Creep) {
        val fillableStructures = findFillStructures(creep = creep)

        if (fillableStructures.isEmpty()) {
            ScreepsLog.d(TAG, "No structures to fill with energy")
            return
        }

        val structureType = (fillableStructures[0] as Structure).structureType
        val fillableStructure = if (structureType == STRUCTURE_TOWER) {
            fillableStructures.maxByOrNull { it.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0 }
        } else {
            fillableStructures.minByOrNull { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
        }

        if (fillableStructure == null) {
            ScreepsLog.d(TAG, "No structures to fill with energy!")
            return
        }

        val status = creep.transfer(fillableStructure, RESOURCE_ENERGY)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(fillableStructure)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            creep.say("Transfer failed with code $status")
        }

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }
}
