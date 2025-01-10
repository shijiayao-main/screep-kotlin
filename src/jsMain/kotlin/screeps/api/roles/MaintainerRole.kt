package screeps.api.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_STRUCTURES
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_WALL
import screeps.api.compareTo
import screeps.api.entity.RoomInfo
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.setState

class MaintainerRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo,
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {
    companion object {
        private const val TAG = "MaintainerRole"
    }

    override fun startWork() {
        creepList.forEach { creep: Creep ->
            run(creep = creep)
        }
    }

    private fun run(creep: Creep) {
        when (val state = creep.getState()) {
            CreepState.GetEnergy -> {
                getEnergy(creep)
                if (creep.store.getFreeCapacity() == 0) {
                    creep.say("Energy full")
                    creep.setState(CreepState.DoWork)
                }
            }

            CreepState.DoWork -> {
                repairBuildings(creep = creep)
            }
        }
    }

    private fun getEnergy(creep: Creep) {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            pickupEnergy(creep = creep)
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun repairBuildings(creep: Creep) {
        var building =
            creep.room.find(FIND_STRUCTURES).filter { it.structureType in MAINTENANCE_REQUIRED_BUILDING_TYPES }
                .minByOrNull {
                    val ratio = it.hits.toFloat() / it.hitsMax.toFloat()

                    // Chunk float into multiple levels so the creep is less sensitive to repair progress
                    // this makes the creeps focus on repairing a single target until it moves into the next "bucket"
                    (ratio * 1000).toInt()
                }

        if (building == null) {
            ScreepsLog.d(TAG, "No available buildings to repair!")
            return
        }

        if (building.hits.toFloat() / building.hitsMax.toFloat() > 0.90) {
            val wall = creep.room.find(FIND_STRUCTURES)
                .filter { it.structureType == STRUCTURE_WALL || it.structureType == STRUCTURE_RAMPART }
                .minByOrNull {
                    val ratio = it.hits.toFloat() / it.hitsMax.toFloat()
                    // Chunk float into multiple levels so the creep is less sensitive to repair progress
                    // this makes the creeps focus on repairing a single target until it moves into the next "bucket"
                    (ratio * (it.hitsMax / creep.store.getCapacity(RESOURCE_ENERGY)!!)).toInt()
                }
            if (wall != null) {
                ScreepsLog.d(TAG, "Buildings well maintained, repairing $wall instead")
                building = wall
            }
        }

        val status = creep.repair(building)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(building)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            creep.say("Repair failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }
}
