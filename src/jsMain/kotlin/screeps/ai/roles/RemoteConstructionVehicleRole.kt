package screeps.ai.roles

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.ERR_FULL
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_MY_SPAWNS
import screeps.api.FIND_SOURCES
import screeps.api.Flag
import screeps.api.FlagMemory
import screeps.api.Game
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.compareTo
import screeps.api.get
import screeps.sdk.ScreepsLog
import screeps.sdk.utils.getPathToTarget
import screeps.utils.memory.memory
import kotlin.math.abs

var FlagMemory.complete: Boolean by memory { false }

class RemoteConstructionVehicleRole(creep: Creep) : AbstractRole(creep) {

    companion object {
        private const val TAG = "RemoteConstructionVehicleRole"
    }

    private val targetFlag: Flag? = Game.flags["NextRoom"]

    override fun run() {
        if (targetFlag == null) {
            ScreepsLog.d(TAG, "No target to work with!")
        } else {
            if (targetFlag.room?.find(FIND_MY_SPAWNS)?.firstOrNull() != null) {
                ScreepsLog.d(TAG, "Spawn construction completed!")
                targetFlag.memory.complete = true
            }
            // Move to target room if not in room
            if (creep.room != targetFlag.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, targetFlag.pos)[0]))
                return
            }
        }

        if (state == CreepState.GET_ENERGY) {
            getEnergy()
        } else if (state == CreepState.DO_WORK) {
            buildBuildings()
        }
    }

    private fun getEnergy() {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            ScreepsLog.d(TAG, "No storage in room, going to gather instead")
            gatherEnergy()
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun gatherEnergy() {
        val energySource =
            creep.room.find(FIND_SOURCES).sortedBy { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
                .firstOrNull { it.energy > 0 } ?: return say("No energy available!")

        val code = creep.harvest(energySource)

        if (code == ERR_NOT_IN_RANGE) {
            if (creep.store.getUsedCapacity(RESOURCE_ENERGY) > 50) {
                state = CreepState.DO_WORK
            } else {
                creep.moveTo(energySource)
            }
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Gather failed with code $code")
        }

        if ((creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            state = CreepState.DO_WORK
        }
    }

    private fun buildBuildings() {
        val constructionSite = findConstructionSite()

        if (constructionSite == null) {
            say("No available construction site!")
            depositEnergy()
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            if (creep.room != constructionSite.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, constructionSite.pos)[0]))
            } else {
                creep.moveTo(constructionSite)
            }
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

    private fun findConstructionSite(): ConstructionSite? {
        return if (targetFlag != null && creep.room != targetFlag.room) {
            targetFlag.room?.find(FIND_CONSTRUCTION_SITES)?.firstOrNull()
        } else {
            creep.room.find(FIND_CONSTRUCTION_SITES)
                .minByOrNull { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
        }
    }

    private fun depositEnergy() {
        val spawner = creep.room.find(FIND_MY_SPAWNS).firstOrNull() ?: let {
            ScreepsLog.d(TAG, "No spawner to deposit energy into")
            return
        }

        val code = creep.transfer(spawner, RESOURCE_ENERGY)

        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawner)
        } else if (code == ERR_NOT_ENOUGH_ENERGY) {
            say("Out of energy")
            state = CreepState.GET_ENERGY
            return
        } else if (code == ERR_FULL) {
            ScreepsLog.d(TAG, "Spawner full of energy, dropping energy for other creeps to use")
            creep.drop(RESOURCE_ENERGY)
            state = CreepState.GET_ENERGY
        } else if (code != OK) {
            say("Transfer failed with code $code")
        }

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}
