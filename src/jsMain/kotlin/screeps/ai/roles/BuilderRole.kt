package screeps.ai.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_STRUCTURES
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.STRUCTURE_RAMPART
import screeps.api.STRUCTURE_WALL
import screeps.api.compareTo
import screeps.sdk.ScreepsLog

class BuilderRole(creep: Creep) : AbstractRole(creep) {

    companion object {
        private const val TAG = "BuilderRole"
    }

    override fun run() {
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
                if (creep.store.getFreeCapacity() == 0) {
                    say("Energy full")
                    state = CreepState.DO_WORK
                }
            }

            CreepState.DO_WORK -> {
                buildBuildings()
            }
        }
    }

    private fun getEnergy() {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            pickupEnergy()
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun buildBuildings() {
        val constructionSite = creep.pos.findClosestByPath(FIND_CONSTRUCTION_SITES)

        if (constructionSite == null) {
            ScreepsLog.d(TAG, "No available construction sites!")
            // Fall back to repairing buildings if there are none that need to be built
            repairBuildings()
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(constructionSite)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            say("Out of energy")
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            say("Build failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }

    private fun repairBuildings() {
        val building =
            creep.room.find(FIND_STRUCTURES)
                .filter { it.structureType in MAINTENANCE_REQUIRED_BUILDING_TYPES || it.structureType == STRUCTURE_WALL || it.structureType == STRUCTURE_RAMPART }
                .minByOrNull {
                    val ratio = it.hits.toFloat() / it.hitsMax.toFloat()

                    // Chunk float into multiple levels so the creep is less sensitive to repair progress
                    // this makes the creeps focus on repairing a single target until it moves into the next "bucket"
                    (ratio * 1000).toInt()
                }

        if (building == null) {
            say("No available buildings to repair!")
            return
        }

        val status = creep.repair(building)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(building)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            say("Out of energy")
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            say("Repair failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}
